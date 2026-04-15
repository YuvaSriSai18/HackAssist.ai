package com.hackassist.ai.models.evaluation;

public class EvaluationResult {
    private String status;
    private int completionPercentage;
    private double confidence;

    public EvaluationResult() {}

    public EvaluationResult(String status, int completionPercentage, double confidence) {
        this.status = status;
        this.completionPercentage = completionPercentage;
        this.confidence = confidence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(int completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
