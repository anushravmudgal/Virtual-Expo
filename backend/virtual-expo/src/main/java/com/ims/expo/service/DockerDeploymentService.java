package com.ims.expo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.ims.expo.entity.Deployment;
import com.ims.expo.entity.DeploymentStatus;
import com.ims.expo.entity.StudentGroup;
import com.ims.expo.repository.DeploymentRepository;
import com.ims.expo.repository.StudentGroupRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerDeploymentService {

    private final DeploymentRepository deploymentRepository;
    private final StudentGroupRepository studentGroupRepository; 

    @Value("${github.classroom.token}")
    private String githubToken;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private String getProjectFootprint(File dir) {
        try (Stream<Path> paths = Files.walk(dir.toPath(), 2)) {
            return paths
                .filter(Files::isRegularFile)
                .map(path -> dir.toPath().relativize(path).toString())
                .filter(name -> !name.contains(".git") && !name.contains("node_modules"))
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Unknown structure";
        }
    }

    // UPDATED: Now prompts the AI for Docker Compose
    private String askAIToGenerateCompose(String footprint) {
        String prompt = "You are an expert DevOps engineer. I have a student's full-stack project with this file structure:\n" +
                        footprint + "\n\n" +
                        "Write a single `docker-compose.yml` file to run this application. " +
                        "1. Analyze the footprint to determine if they need MySQL, MongoDB, or Postgres, and include that database as a service named 'database'. Set a default root password to 'password'.\n" +
                        "2. Include the backend service named 'backend'. It should build from the backend directory.\n" +
                        "3. Include the frontend service named 'frontend'. Use a standard build approach for their framework.\n" +
                        "4. CRITICAL: Add `mem_limit: 256m` to every single service to prevent server crashes.\n" +
                        "5. CRITICAL: For the 'frontend' service, expose its port dynamically so Docker assigns a random host port. DO NOT bind to a specific host port. Use the syntax `ports: - '3000'` or `ports: - '80'` (depending on the framework). STRICTLY DO NOT use colons like `ports: - '3000:3000'`. \\n" +
                        "Return ONLY the raw YAML text. Do not use markdown formatting like ```yaml. No explanations.";

        String url = "[https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=](https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=)" + geminiApiKey;

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        try {
            String responseString = restTemplate.postForObject(url, request, String.class);
            
            if (responseString != null) {
                JsonNode response = mapper.readTree(responseString);
                
                if (response.has("candidates")) {
                    String rawYaml = response.path("candidates").get(0)
                                                   .path("content")
                                                   .path("parts").get(0)
                                                   .path("text").asText();
                    
                    return rawYaml.replace("```yaml", "").replace("```", "").trim();
                }
            }
            throw new RuntimeException("Invalid response from Gemini API: " + responseString);
            
        } catch (Exception e) {
            log.error("Failed to generate docker-compose using AI: {}", e.getMessage());
            throw new RuntimeException("AI DevOps generation failed: " + e.getMessage());
        }
    }

    @Async
    public void buildAndDeployProject(Deployment deployment, StudentGroup group) {
        File cloneDir = new File(System.getProperty("java.io.tmpdir"), "expo-builds/" + deployment.getId());
        
        try {
            updateStatus(deployment, DeploymentStatus.BUILDING, null);

            String originalUrl = group.getGithubUrl();
            String authUrl = originalUrl;
            
            // Fixed the URL parsing logic from the previous phantom brackets
            if (originalUrl != null && originalUrl.startsWith("[https://github.com/](https://github.com/)")) {
                authUrl = originalUrl.replace("[https://github.com/](https://github.com/)", "https://" + githubToken + "@[github.com/](https://github.com/)");
            }

            log.info("Cloning private repository: {}", originalUrl);
   
            ProcessBuilder pb = new ProcessBuilder("git", "clone", authUrl, cloneDir.getAbsolutePath());
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new RuntimeException("Git clone failed. Check if the PAT is valid or if the repo exists.");
            }

            // --- THE ORCHESTRATION UPGRADE ---
            
            String projectName = "expo-group-" + deployment.getId().toLowerCase();
            File composeFile = new File(cloneDir, "docker-compose.yml");

            if (!composeFile.exists()) {
                log.info("No docker-compose.yml found. Engaging AI DevOps Engineer (Gemini)...");
                String footprint = getProjectFootprint(cloneDir);
                log.info("Project Footprint: \n{}", footprint);
                
                String aiGeneratedCompose = askAIToGenerateCompose(footprint);
                Files.writeString(composeFile.toPath(), aiGeneratedCompose);
                
                log.info("AI successfully generated and saved the docker-compose.yml.");
                log.debug("Generated YAML contents:\n{}", aiGeneratedCompose);
            } else {
                log.info("Student provided a docker-compose.yml. Proceeding with standard build...");
            }

            // CLEANUP: Tear down old Compose environments
            log.info("Checking for previous running deployments to clean up...");
            studentGroupRepository.findById(group.getId()).ifPresent(currentGroup -> {
                deploymentRepository.findByGroupIdAndStatus(currentGroup.getId(), DeploymentStatus.RUNNING)
                    .ifPresent(oldDeployment -> {
                        try {
                            log.info("Tearing down old environment: {}", oldDeployment.getContainerId());
                            // Runs: docker-compose -p <projectName> down -v
                            ProcessBuilder downPb = new ProcessBuilder("docker-compose", "-p", oldDeployment.getContainerId(), "down", "-v");
                            downPb.start().waitFor();
                            
                            oldDeployment.setStatus(DeploymentStatus.STOPPED);
                            deploymentRepository.save(oldDeployment);
                            log.info("Old environment cleaned up successfully.");
                        } catch (Exception e) {
                            log.warn("Could not stop old environment. It might already be dead: {}", e.getMessage());
                        }
                    });
            });
// --- THE PAAS SAFEGUARD: SANITIZE PORTS ---
            try {
                String composeContent = Files.readString(composeFile.toPath());
                
                // Regex checks for 'HOST:CONTAINER' patterns and strips the 'HOST:' part
                // Transforms `- "3000:3000"` into `- "3000"`
                String sanitizedContent = composeContent.replaceAll("-\\s*[\"']?\\d+:(\\d+)[\"']?", "- \"$1\"");
                
                if (!composeContent.equals(sanitizedContent)) {
                    Files.writeString(composeFile.toPath(), sanitizedContent);
                    log.info("Sanitized docker-compose.yml to enforce dynamic host ports. Removed hardcoded bindings.");
                }
            } catch (Exception e) {
                log.warn("Could not sanitize compose file, proceeding anyway: {}", e.getMessage());
            }

            // SPIN UP: Run Docker Compose
            log.info("Spinning up multi-container environment via Docker Compose...");
            ProcessBuilder composePb = new ProcessBuilder("docker-compose", "-p", projectName, "up", "-d", "--build");
            composePb.directory(cloneDir);
            composePb.redirectErrorStream(true);
            Process composeProcess = composePb.start();
            
            String composeLogs = new String(composeProcess.getInputStream().readAllBytes());
            int composeExitCode = composeProcess.waitFor();
            
            if (composeExitCode != 0) {
                log.error("Docker Compose failed: \n{}", composeLogs);
                throw new RuntimeException("Compose build failed. Logs: " + composeLogs);
            }

            // RETRIEVE DYNAMIC PORT
            log.info("Retrieving dynamically assigned port...");
            
            // Give Docker 2 seconds to fully register the port bindings before checking
            Thread.sleep(2000); 
            
            // Compose automatically names the frontend container like this:
            String containerName = projectName + "-frontend-1"; 
            
            ProcessBuilder portPb = new ProcessBuilder("docker", "port", containerName);
            Process portProcess = portPb.start();
            String portOutput = new String(portProcess.getInputStream().readAllBytes()).trim(); 
            // Expecting output like: "3000/tcp -> 0.0.0.0:32769"
            
            int assignedHostPort = 80; // Fallback
            if (portOutput.contains(":")) {
                // Extract the final port number from the output string
                String firstLine = portOutput.split("\n")[0];
                assignedHostPort = Integer.parseInt(firstLine.substring(firstLine.lastIndexOf(":") + 1).trim());
            } else {
                throw new RuntimeException("Could not detect assigned port. Ensure the AI exposed the frontend. Docker output: " + portOutput);
            }

            // Save the Compose Project Name instead of a single Container ID
            deployment.setContainerId(projectName);
            deployment.setInternalPort(assignedHostPort);
            
            StudentGroup groupToUpdate = deployment.getGroup(); 
            if (groupToUpdate != null) {
                groupToUpdate.setLiveUrl("http://localhost:" + assignedHostPort);
                studentGroupRepository.save(groupToUpdate); 
            }
            
            updateStatus(deployment, DeploymentStatus.RUNNING, null);
            log.info("Deployment Successful! Frontend running on port: {}", assignedHostPort);

        } catch (Exception e) {
            log.error("Deployment failed", e);
            updateStatus(deployment, DeploymentStatus.FAILED, e.getMessage());
        } finally {
            deleteDirectory(cloneDir);
        }
    }

    private void updateStatus(Deployment deployment, DeploymentStatus status, String errorLog) {
        deployment.setStatus(status);
        if (errorLog != null) deployment.setErrorLogs(errorLog);
        deploymentRepository.save(deployment);
    }

    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}