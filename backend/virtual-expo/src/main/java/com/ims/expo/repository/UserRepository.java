package com.ims.expo.repository;

import com.ims.expo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    // Used during GitHub login to find existing students
    Optional<User> findByGithubId(String githubId);
    
    // Standard email lookup
    Optional<User> findByEmail(String email);
}