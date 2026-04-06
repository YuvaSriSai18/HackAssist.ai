package com.hackassist.ai.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "github_users")
public class GitHubUser {

    @Id
    @Column(nullable = false)
    private String githubId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "uid", referencedColumnName = "uid", nullable = false)
    private User user;

    @Column(nullable = false)
    private String githubUsername;

    private String name;

    private String avatarUrl;

    @Column(nullable = false)
    private boolean githubConnected = true;


    @Column(nullable = false)
    private boolean githubVerified = true;

    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public String getUid() {
        return user == null ? null : user.getUid();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public String getGithubId() {
        return githubId;
    }

    public void setGithubId(String githubId) {
        this.githubId = githubId;
    }

    public boolean isGithubVerified() {
        return githubVerified;
    }

    public void setGithubVerified(boolean githubVerified) {
        this.githubVerified = githubVerified;
    }

    public boolean isGithubConnected() {
        return githubConnected;
    }

    public void setGithubConnected(boolean githubConnected) {
        this.githubConnected = githubConnected;
    }


    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
