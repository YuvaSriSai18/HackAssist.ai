package com.hackassist.ai.Service;

import com.hackassist.ai.dto.plan.ProjectPlanDTO;

public interface IAIService {
    ProjectPlanDTO generatePlan(String projectId, String problemStatement, String userId);
}
