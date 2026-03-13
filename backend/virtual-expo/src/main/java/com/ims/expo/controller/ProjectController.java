package com.ims.expo.controller;

import com.ims.expo.entity.Project;
import com.ims.expo.repository.ProjectRepository;
import com.ims.expo.repository.StudentGroupRepository;
import com.ims.expo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*") // Allows your future React/Angular app to call this API
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final StudentGroupRepository groupRepository;

    // 1. PUBLIC VIEW: Fetch only published projects for the main gallery
    @GetMapping
    public ResponseEntity<List<Project>> getPublicProjects() {
        return ResponseEntity.ok(projectRepository.findByIsPublicTrueOrderByCreatedAtDesc());
    }

    // 2. STUDENT DASHBOARD: Fetch all projects (including drafts) for a specific student
  @GetMapping("/user/{userId}")
    public ResponseEntity<List<Project>> getStudentProjects(@PathVariable String userId) {
        // We now call the new custom query method!
        return ResponseEntity.ok(projectRepository.findProjectsByStudentId(userId)); 
    }

    // 3. CREATE PROJECT: Create a new project entry
   @PostMapping("/{groupId}")
    public ResponseEntity<Project> createProject(@PathVariable String groupId, @RequestBody Project project) {
        return groupRepository.findById(groupId).map(group -> { // <--- Changed from userRepository
            project.setGroup(group);                            // <--- Changed from setUser
            return ResponseEntity.ok(projectRepository.save(project));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. PUBLISH TOGGLE: Flip a project from Private to Public
    @PatchMapping("/{projectId}/publish")
    public ResponseEntity<Project> togglePublish(@PathVariable String projectId, @RequestParam boolean isPublic) {
        return projectRepository.findById(projectId).map(project -> {
            project.setPublic(isPublic);
            return ResponseEntity.ok(projectRepository.save(project));
        }).orElse(ResponseEntity.notFound().build());
    }
}