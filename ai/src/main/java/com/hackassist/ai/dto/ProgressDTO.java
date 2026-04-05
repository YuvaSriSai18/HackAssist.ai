package com.hackassist.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDTO {
    private Long projectId;
    private Double taskCompletionPercentage;
    private Double commitContributionPercentage;
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer totalCommits;
    private Integer totalTeamMembers;
    private String overallHealthStatus;
}
