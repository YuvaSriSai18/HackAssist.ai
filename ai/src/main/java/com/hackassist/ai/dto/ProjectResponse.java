package com.hackassist.ai.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String projectId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private String githubRepoUrl;
}
