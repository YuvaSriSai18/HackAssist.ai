package com.hackassist.ai.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_repository_mapping", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"repo_full_name"})
})
public class ProjectRepositoryMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "repo_full_name", nullable = false)
    private String repoFullName;

    @Column(name = "webhook_id", nullable = true)
    private Long webhookId;

    @Column(name = "webhook_enabled", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean webhookEnabled = false;

    @Column(name = "webhook_created_at", nullable = true)
    private LocalDateTime webhookCreatedAt;

    public ProjectRepositoryMapping() {}

    public ProjectRepositoryMapping(Long projectId, String repoFullName) {
        this.projectId = projectId;
        this.repoFullName = repoFullName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getRepoFullName() {
        return repoFullName;
    }

    public void setRepoFullName(String repoFullName) {
        this.repoFullName = repoFullName;
    }

    public Long getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(Long webhookId) {
        this.webhookId = webhookId;
    }

    public Boolean getWebhookEnabled() {
        return webhookEnabled;
    }

    public void setWebhookEnabled(Boolean webhookEnabled) {
        this.webhookEnabled = webhookEnabled;
    }

    public LocalDateTime getWebhookCreatedAt() {
        return webhookCreatedAt;
    }

    public void setWebhookCreatedAt(LocalDateTime webhookCreatedAt) {
        this.webhookCreatedAt = webhookCreatedAt;
    }
}
