package com.ims.expo.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ims.expo.entity.StudentGroup;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, String> {

    boolean existsByName(String name); // <-- Added missing semicolon!

    // Finds the group where a specific student is either a leader or a member
    @EntityGraph(attributePaths = {"leader", "members"})
    Optional<StudentGroup> findByMembers_Id(String userId);

    @EntityGraph(attributePaths = {"leader", "members"})
    List<StudentGroup> findAll();
}