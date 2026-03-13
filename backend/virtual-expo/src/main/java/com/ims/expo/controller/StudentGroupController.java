package com.ims.expo.controller;

import com.ims.expo.entity.GroupMember;
import com.ims.expo.entity.StudentGroup;
import com.ims.expo.repository.GroupMemberRepository;
import com.ims.expo.repository.StudentGroupRepository;
import com.ims.expo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class StudentGroupController {

    private final StudentGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    @GetMapping
    public ResponseEntity<List<StudentGroup>> getAllGroups() {
        return ResponseEntity.ok(groupRepository.findAll());
    }

    // CREATE A NEW GROUP (Or a "Solo" group)
    @PostMapping("/create/{leaderId}")
    public ResponseEntity<?> createGroup(@PathVariable String leaderId, @RequestParam String groupName, @RequestParam String role) {
        if (groupRepository.existsByName(groupName)) {
            return ResponseEntity.badRequest().body("Group name already taken!");
        }

        return userRepository.findById(leaderId).map(leader -> {
            // 1. Create the Group
            StudentGroup group = new StudentGroup();
            group.setName(groupName);
            group.setLeader(leader);
            StudentGroup savedGroup = groupRepository.save(group);

            // 2. Add the leader to the group_members join table
            GroupMember member = new GroupMember();
            member.setGroup(savedGroup);
            member.setUser(leader);
            member.setProjectRole(role); // e.g., "Full Stack Developer"
            groupMemberRepository.save(member);

            return ResponseEntity.ok(savedGroup);
        }).orElse(ResponseEntity.notFound().build());
    }

    // JOIN AN EXISTING GROUP
 @PostMapping("/{groupId}/join/{userId}")
    public ResponseEntity<?> joinGroup(
            @PathVariable String groupId, 
            @PathVariable String userId, 
            @RequestParam String role) { // 'role' here is the project role (e.g., Frontend Developer)
            
        return groupRepository.findById(groupId).map(group -> {
            return userRepository.findById(userId).map(user -> {
                
                // 1. Check if the user is already inside this specific group
                // Note: group.getMembers() gives us GroupMember objects now, so we must call getUser()
                boolean alreadyInGroup = group.getMembers().stream()
                        .anyMatch(member -> member.getUser().getId().equals(user.getId()));
                        
                if (alreadyInGroup) {
                    return ResponseEntity.badRequest().body("You are already a member of this team!");
                }
                
                // 2. Create the Join Entity!
                GroupMember newMember = new GroupMember();
                newMember.setUser(user);
                newMember.setGroup(group);
                newMember.setProjectRole(role); // Sets "Frontend Developer" safely!
                
                // 3. Add to the group and save
                group.getMembers().add(newMember);
                groupRepository.save(group);
                
                return ResponseEntity.ok("Successfully joined the team!");
                
            }).orElse(ResponseEntity.badRequest().body("User not found."));
        }).orElse(ResponseEntity.badRequest().body("Group not found."));
    }

    @PutMapping("/{groupId}/project")
    public ResponseEntity<?> submitProjectDetails(
            @PathVariable String groupId, 
            @RequestBody StudentGroup projectData) { 
            
        return groupRepository.findById(groupId).map(group -> {
            
            // Only save the Title and the Code Repository!
            group.setProjectTitle(projectData.getProjectTitle());
            group.setGithubUrl(projectData.getGithubUrl());
            
            groupRepository.save(group);
            return ResponseEntity.ok(group);
            
        }).orElse(ResponseEntity.notFound().build());
    }

    // ADMIN ONLY: Delete a project/team
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable String id) {
        groupRepository.deleteById(id);
        return ResponseEntity.ok("{\"message\": \"Group deleted successfully\"}");
    }

    // FETCH A SINGLE PROJECT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupById(@PathVariable String id) {
        return groupRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}