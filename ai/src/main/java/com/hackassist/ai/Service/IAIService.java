package com.hackassist.ai.Service;

import com.hackassist.ai.dto.AITasksResponseDTO;
import com.hackassist.ai.dto.FeatureDTO;
import java.util.List;

public interface IAIService {
    
    AITasksResponseDTO generateTasksFromProblemStatement(String problemStatement, String hackathonTheme);
    
    List<FeatureDTO> generateFeatures(String problemStatement);
    
    String generateProjectSummary(Long projectId);
}
