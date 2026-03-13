package com.ims.expo.repository;

import com.ims.expo.entity.ProjectLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectLikeRepository extends JpaRepository<ProjectLike, String> {
    
    // Counts the total likes to display on the project card
    long countByProjectId(String projectId);
    
    // Checks if a user has already liked a project (so we can toggle the Like button color)
    boolean existsByProjectIdAndUserId(String projectId, String userId);
    
    // Used to delete a like if the user "un-likes" it
    void deleteByProjectIdAndUserId(String projectId, String userId);
}