package com.hackassist.ai.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_evaluations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"task_id"})
})
public class TaskEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Integer progress;

    @Column(name = "last_evaluated_commit_sha")
    private String lastEvaluatedCommitSha;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "last_evaluated_at", nullable = false)
    private LocalDateTime lastEvaluatedAt;

    public TaskEvaluation() {}

    public TaskEvaluation(Long taskId, Long projectId) {
        this.taskId = taskId;
        this.projectId = projectId;
        this.progress = 0;
        this.lastEvaluatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastEvaluatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getLastEvaluatedCommitSha() {
        return lastEvaluatedCommitSha;
    }

    public void setLastEvaluatedCommitSha(String lastEvaluatedCommitSha) {
        this.lastEvaluatedCommitSha = lastEvaluatedCommitSha;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public LocalDateTime getLastEvaluatedAt() {
        return lastEvaluatedAt;
    }

    public void setLastEvaluatedAt(LocalDateTime lastEvaluatedAt) {
        this.lastEvaluatedAt = lastEvaluatedAt;
    }
}
