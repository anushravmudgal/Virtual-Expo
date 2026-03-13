package com.ims.expo.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "deployments")
@Getter @Setter @NoArgsConstructor
public class Deployment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // This is the "Workspace". It handles both Solo and Group projects.
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "group_id", nullable = false) 
    private StudentGroup group;

    @Enumerated(EnumType.STRING)
    private DeploymentStatus status = DeploymentStatus.QUEUED;

    private String containerId;
    private Integer internalPort; // The dynamic port assigned by your Docker engine
    
    @Lob // Tells JPA this is a Large Object
    @Column(columnDefinition = "LONGTEXT") // Forces MySQL to use LONGTEXT (up to 4GB)
    private String errorLogs;

    @CreationTimestamp
    private LocalDateTime deployedAt;

    @Column(unique = true)
     private String subdomain;

    // HELPER: Updates the main project URL after a successful build
    public String getGeneratedUrl() {
        return "http://localhost:" + internalPort;
    }
}