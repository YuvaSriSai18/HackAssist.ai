package com.hackassist.ai.dto.plan;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPlanDTO {
    private String projectId;
    private String problemStatement;
    private TechStackDTO techStack;
    private List<FeatureDTO> features;
    private List<ModuleDTO> modules;
    private List<TaskDTO> tasks;
    private List<RiskDTO> risks;
}
