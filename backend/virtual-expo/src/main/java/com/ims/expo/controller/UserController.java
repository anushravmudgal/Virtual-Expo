package com.ims.expo.controller;

import com.ims.expo.entity.User;
import com.ims.expo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    // STEP 2 OF REGISTRATION: Complete the student profile
    @PutMapping("/{userId}/profile")
    public ResponseEntity<User> completeProfile(@PathVariable String userId, @RequestBody User profileData) {
        return userRepository.findById(userId).map(user -> {
            
            // 1. Update the email they provided in the form
            if (profileData.getEmail() != null && !profileData.getEmail().trim().isEmpty()) {
                user.setEmail(profileData.getEmail());
            }
            
            // 2. The Gatekeeper Re-Check!
            if (user.getEmail().endsWith("@imsnoida.com")) {
                user.setApproved(true); // Auto-approve them!
            }
            
            // 3. Update placement details
            user.setRollNumber(profileData.getRollNumber());
            user.setCourse(profileData.getCourse()); 
            user.setSemester(profileData.getSemester());
            user.setSection(profileData.getSection());
            user.setSkills(profileData.getSkills());
            user.setPreferredLanguage(profileData.getPreferredLanguage());
            user.setTargetItDomain(profileData.getTargetItDomain());
            user.setLinkedinUrl(profileData.getLinkedinUrl());
            user.setDreamJobProfile(profileData.getDreamJobProfile());
            user.setIndustry(profileData.getIndustry());
            user.setTargetCompanies(profileData.getTargetCompanies());
            
            user.setProfileComplete(true);
            
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    // UPDATE STUDENT PROFILE (Locked Identity Fields)
    @PutMapping("/{userId}/portfolio")
    public ResponseEntity<?> updateStudentProfile(
            @PathVariable String userId, 
            @RequestBody User updatedData) {
            
        return userRepository.findById(userId).map(user -> {
            // We ONLY update the professional/portfolio fields.
            // Name, Roll Number, Course, and Email are strictly ignored!
            
            user.setPhotoUrl(updatedData.getPhotoUrl());
            user.setIntroduction(updatedData.getIntroduction());
            user.setSkills(updatedData.getSkills());
            user.setPreferredLanguage(updatedData.getPreferredLanguage());
            user.setLinkedinUrl(updatedData.getLinkedinUrl());
            user.setResumeUrl(updatedData.getResumeUrl());
            user.setDreamJobProfile(updatedData.getDreamJobProfile());
            user.setIndustry(updatedData.getIndustry());
            user.setTargetCompanies(updatedData.getTargetCompanies());
            user.setHobbies(updatedData.getHobbies());
            user.setCertifications(updatedData.getCertifications());
            
            userRepository.save(user);
            return ResponseEntity.ok(user);
            
        }).orElse(ResponseEntity.notFound().build());
    }

    // FETCH A STUDENT'S ACTIVE WORKSPACE
    @GetMapping("/{userId}/group")
    public ResponseEntity<?> getUserGroup(@PathVariable String userId) {
        return userRepository.findById(userId).map(user -> {
            
            // Check if the student actually belongs to a group
            if (user.getGroupMemberships() != null && !user.getGroupMemberships().isEmpty()) {
                // Grab the group from their first membership link
                return ResponseEntity.ok(user.getGroupMemberships().get(0).getGroup());
            }
            
            // If they aren't in a group, send back an empty response so Angular knows to show the Join screen
            return ResponseEntity.noContent().build(); 
            
        }).orElse(ResponseEntity.notFound().build());
    }

    // 1. UPLOAD RESUME ENDPOINT
    @PostMapping("/{userId}/resume")
    public ResponseEntity<?> uploadResume(
            @PathVariable String userId, 
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return ResponseEntity.badRequest().body("File is empty");

            // Create an 'uploads/resumes' folder in your project directory if it doesn't exist
            Path uploadPath = Paths.get("uploads/resumes/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate a safe, unique filename (e.g., "userId_resume.pdf")
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = userId + "_resume" + extension;
            
            // Save the file to the hard drive
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Save the local download URL to the database
            return userRepository.findById(userId).map(user -> {
                String downloadUrl = "http://localhost:8088/api/users/resume/download/" + newFilename;
                user.setResumeUrl(downloadUrl);
                userRepository.save(user);
                
                return ResponseEntity.ok("{\"message\": \"Resume uploaded successfully\", \"resumeUrl\": \"" + downloadUrl + "\"}");
            }).orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to upload file: " + e.getMessage());
        }
    }

    // 2. DOWNLOAD RESUME ENDPOINT (For Recruiters later)
    @GetMapping("/resume/download/{filename}")
    public ResponseEntity<Resource> downloadResume(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads/resumes/").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // FETCH ALL COMPLETED STUDENT PROFILES FOR THE DIRECTORY
    @GetMapping
    public ResponseEntity<?> getAllStudents() {
        // We fetch everyone, but filter out admins, viewers, and incomplete profiles
        return ResponseEntity.ok(userRepository.findAll().stream()
                .filter(user -> user.isProfileComplete() && "STUDENT".equals(user.getRole().name()))
                .toList());
    }

    // ADMIN ONLY: Delete a student
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("{\"message\": \"User deleted successfully\"}");
    }
}