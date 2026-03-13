package com.ims.expo.controller;

import com.ims.expo.entity.Deployment;
import com.ims.expo.entity.DeploymentStatus;
import com.ims.expo.entity.StudentGroup;
import com.ims.expo.service.DockerDeploymentService;
import com.ims.expo.repository.DeploymentRepository;
import com.ims.expo.repository.StudentGroupRepository; // Updated Repository
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; // Fixes: cannot find symbol Map

@RestController
@RequestMapping("/api/deployments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DeploymentController {

    private final DockerDeploymentService dockerDeploymentService;
    private final DeploymentRepository deploymentRepository;
    private final StudentGroupRepository studentGroupRepository; // Fixes: variable groupRepository not found

    // 1. Get deployment history for a workspace
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Deployment>> getGroupDeployments(@PathVariable String groupId) {
        return ResponseEntity.ok(deploymentRepository.findByGroupIdOrderByDeployedAtDesc(groupId));
    }

    // 2. Trigger a new deployment (The PaaS Engine)
   @PostMapping("/{groupId}/trigger")
public ResponseEntity<?> triggerDeployment(@PathVariable String groupId){
        // Fixes: incompatible types (Project vs StudentGroup)
        return studentGroupRepository.findById(groupId).map(group -> {
            
            Deployment deployment = new Deployment();
            deployment.setGroup(group); // Matches your Deployment.java field
            deployment.setStatus(DeploymentStatus.QUEUED);
            deployment.setDeployedAt(LocalDateTime.now());
            
            // Note: If you removed 'subdomain' or 'commitHash' from the Entity, 
            // ensure you don't call setters for them here.
            
            Deployment savedDeployment = deploymentRepository.save(deployment);
            
            // Pass the group directly to the service
            dockerDeploymentService.buildAndDeployProject(savedDeployment, group);
            
            // Fixes: cannot find symbol Map
            return ResponseEntity.ok(Map.of(
                "id", savedDeployment.getId(),
                "status", "QUEUED",
                "message", "Build process started in background"
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 3. Status Polling Endpoint (For Angular)
    @GetMapping("/status/{id}")
    public ResponseEntity<?> getStatus(@PathVariable String id) {
        return deploymentRepository.findById(id).map(d -> {
            String url = (d.getStatus() == DeploymentStatus.RUNNING) ? "http://localhost:" + d.getInternalPort() : "";
            return ResponseEntity.ok(Map.of(
                "status", d.getStatus(),
                "url", url,
                "error", d.getErrorLogs() != null ? d.getErrorLogs() : ""
            ));
        }).orElse(ResponseEntity.notFound().build());
    }
}