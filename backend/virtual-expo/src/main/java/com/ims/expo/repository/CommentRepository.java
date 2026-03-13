package com.ims.expo.repository;

import com.ims.expo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    
    // Gets all comments for a project, newest first
    List<Comment> findByProjectIdOrderByCreatedAtDesc(String projectId);
}