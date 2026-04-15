package com.hackassist.ai.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_evaluation_state")
public class ProjectEvaluationState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @Column(name = "last_evaluated_commit_sha")
    private String lastEvaluatedCommitSha;

    @Column(name = "last_evaluated_at")
    private LocalDateTime lastEvaluatedAt;

    public ProjectEvaluationState() {}

    public ProjectEvaluationState(Project project) {
        this.project = project;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getLastEvaluatedCommitSha() {
        return lastEvaluatedCommitSha;
    }

    public void setLastEvaluatedCommitSha(String lastEvaluatedCommitSha) {
        this.lastEvaluatedCommitSha = lastEvaluatedCommitSha;
    }

    public LocalDateTime getLastEvaluatedAt() {
        return lastEvaluatedAt;
    }

    public void setLastEvaluatedAt(LocalDateTime lastEvaluatedAt) {
        this.lastEvaluatedAt = lastEvaluatedAt;
    }
}
