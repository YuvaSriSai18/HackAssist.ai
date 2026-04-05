package com.hackassist.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AITasksResponseDTO {
    private String projectName;
    private List<AIGeneratedTaskDTO> tasks;
    private List<FeatureDTO> features;
    private String projectSummary;
    private List<String> recommendedTechnologies;
}
