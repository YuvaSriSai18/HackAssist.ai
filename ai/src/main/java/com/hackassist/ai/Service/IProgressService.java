package com.hackassist.ai.Service;

import com.hackassist.ai.dto.ProgressDTO;

public interface IProgressService {
    
    ProgressDTO getProjectProgress(Long projectId);
    
    Double calculateTaskCompletionPercentage(Long projectId);
    
    Double calculateCommitContribution(Long projectId);
}
