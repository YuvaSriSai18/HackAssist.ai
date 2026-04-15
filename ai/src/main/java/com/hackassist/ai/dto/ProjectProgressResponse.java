package com.hackassist.ai.dto;

import java.util.List;

public class ProjectProgressResponse {
    private double overallProgress;
    private List<TaskProgressItem> tasks;
    private List<ActivityItem> recentActivity;
    private List<String> risks;

    public ProjectProgressResponse() {}

    public ProjectProgressResponse(double overallProgress, List<TaskProgressItem> tasks, List<ActivityItem> recentActivity, List<String> risks) {
        this.overallProgress = overallProgress;
        this.tasks = tasks;
        this.recentActivity = recentActivity;
        this.risks = risks;
    }

    public double getOverallProgress() {
        return overallProgress;
    }

    public void setOverallProgress(double overallProgress) {
        this.overallProgress = overallProgress;
    }

    public List<TaskProgressItem> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskProgressItem> tasks) {
        this.tasks = tasks;
    }

    public List<ActivityItem> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<ActivityItem> recentActivity) {
        this.recentActivity = recentActivity;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks;
    }

    public static class TaskProgressItem {
        private Long taskId;
        private int progress;
        private String status;

        public TaskProgressItem() {}

        public TaskProgressItem(Long taskId, int progress, String status) {
            this.taskId = taskId;
            this.progress = progress;
            this.status = status;
        }

        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class ActivityItem {
        private Long taskId;
        private Integer progress;
        private String lastCommitSha;
        private String updatedAt;

        public ActivityItem() {}

        public ActivityItem(Long taskId, Integer progress, String lastCommitSha, String updatedAt) {
            this.taskId = taskId;
            this.progress = progress;
            this.lastCommitSha = lastCommitSha;
            this.updatedAt = updatedAt;
        }

        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public Integer getProgress() {
            return progress;
        }

        public void setProgress(Integer progress) {
            this.progress = progress;
        }

        public String getLastCommitSha() {
            return lastCommitSha;
        }

        public void setLastCommitSha(String lastCommitSha) {
            this.lastCommitSha = lastCommitSha;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
