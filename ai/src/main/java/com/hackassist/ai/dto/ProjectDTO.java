package com.hackassist.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private String problemStatement;
    private String createdByUid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String githubRepoUrl;
    private String status;
    private String aiGeneratedFeatures;
}
