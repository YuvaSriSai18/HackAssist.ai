package com.hackassist.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryDTO {
    private Long projectId;
    private String projectName;
    private String description;
    private String status;
    private Double completionPercentage;
    private List<TeamMemberActivityDTO> teamActivity;
    private List<RiskAlertDTO> activeRisks;
    private List<String> achievedFeatures;
    private List<String> pendingFeatures;
}
