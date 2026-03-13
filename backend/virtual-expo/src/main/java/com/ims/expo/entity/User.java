package com.ims.expo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "github_id", unique = true)
    private String githubId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true) // Nullable because students using GitHub won't have a password
    @JsonIgnore // Security: Never send the hashed password back to the Angular frontend!
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('STUDENT', 'ADMIN', 'VIEWER') DEFAULT 'VIEWER'")
    private Role role = Role.VIEWER;

    // --- PLACEMENT PORTAL FIELDS ---
    @Column(name = "roll_number", unique = true)
    private String rollNumber;

    @Enumerated(EnumType.STRING)
    private Course course;

    private Integer semester;
    
    private String section;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @Column(name = "target_it_domain")
    private String targetItDomain;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    // --- GATEKEEPER SWITCHES ---
    @Column(name = "is_profile_complete")
    private boolean isProfileComplete = false;

    @Column(name = "is_approved")
    private boolean isApproved = false;

    @Column(name = "dream_job_profile")
    private String dreamJobProfile;

    private String industry;

    @Column(name = "target_companies")
    private String targetCompanies; // We will store the 5 companies as a comma-separated string

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(columnDefinition = "TEXT")
    private String hobbies;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // A user can be part of multiple groups
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GroupMember> groupMemberships;

    // Add these fields inside your StudentGroup class!
    
 


}