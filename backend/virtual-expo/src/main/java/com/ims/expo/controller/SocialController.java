package com.ims.expo.controller;

import com.ims.expo.entity.Comment;
import com.ims.expo.entity.ProjectLike;
import com.ims.expo.repository.CommentRepository;
import com.ims.expo.repository.ProjectLikeRepository;
import com.ims.expo.repository.ProjectRepository;
import com.ims.expo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SocialController {

    private final CommentRepository commentRepository;
    private final ProjectLikeRepository likeRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // Fetch comments for a project
    @GetMapping("/{projectId}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable String projectId) {
        return ResponseEntity.ok(commentRepository.findByProjectIdOrderByCreatedAtDesc(projectId));
    }

    // Add a comment
    @PostMapping("/{projectId}/comments/{userId}")
    public ResponseEntity<Comment> addComment(@PathVariable String projectId, @PathVariable String userId, @RequestBody Comment comment) {
        return projectRepository.findById(projectId).flatMap(project -> 
            userRepository.findById(userId).map(user -> {
                comment.setProject(project);
                comment.setUser(user);
                return ResponseEntity.ok(commentRepository.save(comment));
            })
        ).orElse(ResponseEntity.notFound().build());
    }

    // Get total likes
    @GetMapping("/{projectId}/likes/count")
    public ResponseEntity<Long> getLikeCount(@PathVariable String projectId) {
        return ResponseEntity.ok(likeRepository.countByProjectId(projectId));
    }

    // Toggle a like (Like/Unlike)
    @PostMapping("/{projectId}/likes/{userId}")
    public ResponseEntity<String> toggleLike(@PathVariable String projectId, @PathVariable String userId) {
        if (likeRepository.existsByProjectIdAndUserId(projectId, userId)) {
            likeRepository.deleteByProjectIdAndUserId(projectId, userId);
            return ResponseEntity.ok("Unliked");
        } else {
            return projectRepository.findById(projectId).flatMap(project ->
                userRepository.findById(userId).map(user -> {
                    ProjectLike like = new ProjectLike();
                    like.setProject(project);
                    like.setUser(user);
                    likeRepository.save(like);
                    return ResponseEntity.ok("Liked");
                })
            ).orElse(ResponseEntity.notFound().build());
        }
    }
}