package com.hackassist.ai.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "git_commits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitCommit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String commitHash;
    
    @Column(nullable = false)
    private String author;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(nullable = false)
    private LocalDateTime commitDate;
    
    @ManyToOne
    @JoinColumn(name = "repository_id", nullable = false)
    private GitHubRepository repository;
    
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Tasks mappedTask;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    private Integer additions;
    private Integer deletions;
    private Integer fileChanges;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
