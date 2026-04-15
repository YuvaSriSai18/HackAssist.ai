package com.hackassist.ai.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false, unique = true)
    private String projectId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String problemStatement;
    
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "github_repo_url")
    private String githubRepoUrl;

    @Column(name = "github_repo_locked", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean githubRepoLocked = false;
    
    @Column(name = "project_status")
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;
    
    @Column(name = "ai_features")
    @Lob
    private String aiGeneratedFeatures;
    
    @PrePersist
    protected void onCreate() {
        if (projectId == null || projectId.isBlank()) {
            projectId = UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = ProjectStatus.ACTIVE;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Boolean getGithubRepoLocked() {
        return githubRepoLocked;
    }

    public void setGithubRepoLocked(Boolean githubRepoLocked) {
        this.githubRepoLocked = githubRepoLocked;
    }
}
