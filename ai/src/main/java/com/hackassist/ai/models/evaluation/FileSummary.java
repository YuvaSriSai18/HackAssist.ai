package com.hackassist.ai.models.evaluation;

public class FileSummary {
    private String fileName;
    private String summary;

    public FileSummary() {}

    public FileSummary(String fileName, String summary) {
        this.fileName = fileName;
        this.summary = summary;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
