package com.hackassist.ai.models.evaluation;

public class CommitInfo {
    private String sha;
    private String message;

    public CommitInfo() {}

    public CommitInfo(String sha, String message) {
        this.sha = sha;
        this.message = message;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
