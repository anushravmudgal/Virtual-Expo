package com.ims.expo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_groups")
@Getter
@Setter
@NoArgsConstructor
public class StudentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    // The group leader
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // The students inside the group (with their roles)
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> members;

    // A group can submit multiple projects over time
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Project> projects;

 
       @Column(name = "project_title")
    private String projectTitle;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(columnDefinition = "TEXT")
    private String projectDescription;

    @Column(name = "live_url")
    private String liveUrl;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deployment> deployments = new ArrayList<>();
}