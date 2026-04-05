package com.hackassist.ai.Service;

import com.hackassist.ai.dto.ProjectSummaryDTO;
import com.hackassist.ai.dto.RiskAlertDTO;
import java.util.List;

public interface IIntelligenceService {
    
    List<RiskAlertDTO> detectRisks(Long projectId);
    
    ProjectSummaryDTO generateProjectSummaryForPresentation(Long projectId);
    
    String suggestNextActions(Long projectId);
}
