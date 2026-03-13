package com.ims.expo.repository;

import com.ims.expo.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    
    // For the public "Showcase" wall (only fetches published projects)
    List<Project> findByIsPublicTrueOrderByCreatedAtDesc();
    
    // Custom query to find projects by navigating through the group memberships
    @Query("SELECT p FROM Project p JOIN p.group g JOIN g.members m WHERE m.user.id = :userId ORDER BY p.createdAt DESC")
    List<Project> findProjectsByStudentId(@Param("userId") String userId);
}