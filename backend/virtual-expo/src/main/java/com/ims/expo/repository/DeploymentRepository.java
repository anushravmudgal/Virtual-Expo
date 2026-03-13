package com.ims.expo.repository;

import com.ims.expo.entity.Deployment;
import com.ims.expo.entity.DeploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, String> {

    // 1. Rename findByProjectId to findByGroupId
    List<Deployment> findByGroupIdOrderByDeployedAtDesc(String groupId);

    // 2. Rename this to findByGroupIdAndStatus
    Optional<Deployment> findByGroupIdAndStatus(String groupId, DeploymentStatus status);
    
    // 3. Remove any method that mentions 'projectId' or 'subdomain' 
    // unless those fields exist in your Deployment.java
    
    // Ensures a subdomain (e.g., "rahul-app.ims.edu") isn't already taken
    boolean existsBySubdomain(String subdomain);
    
}