package com.hackassist.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryDTO {
    private Long id;
    private String repoName;
    private String repoUrl;
    private String owner;
    private String description;
    private Long projectId;
    private String userUid;
    private LocalDateTime createdAt;
    private LocalDateTime lastSynced;
    private Integer stars;
    private Integer forks;
    private Integer openIssues;
}
