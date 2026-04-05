package com.hackassist.ai.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hackassist.ai.dto.ProgressDTO;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.Tasks;
import com.hackassist.ai.models.TaskStatus;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.TaskRepository;
import com.hackassist.ai.repository.GitCommitRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProgressService implements IProgressService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private GitCommitRepository gitCommitRepository;
    
    @Override
    public ProgressDTO getProjectProgress(Long projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            throw new RuntimeException("Project not found");
        }
        
        ProgressDTO progressDTO = new ProgressDTO();
        progressDTO.setProjectId(projectId);
        
        Double taskCompletion = calculateTaskCompletionPercentage(projectId);
        Double commitContribution = calculateCommitContribution(projectId);
        
        progressDTO.setTaskCompletionPercentage(taskCompletion);
        progressDTO.setCommitContributionPercentage(commitContribution);
        
        List<Tasks> allTasks = taskRepository.findAll();
        int completedTasks = (int) allTasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
            .count();
        
        progressDTO.setTotalTasks(allTasks.size());
        progressDTO.setCompletedTasks(completedTasks);
        
        progressDTO.setOverallHealthStatus(determineHealthStatus(taskCompletion, commitContribution));
        
        return progressDTO;
    }
    
    @Override
    public Double calculateTaskCompletionPercentage(Long projectId) {
        List<Tasks> allTasks = taskRepository.findAll();
        if (allTasks.isEmpty()) {
            return 0.0;
        }
        
        long completedCount = allTasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
            .count();
        
        return (completedCount * 100.0) / allTasks.size();
    }
    
    @Override
    public Double calculateCommitContribution(Long projectId) {
        // Simplified calculation based on total commits
        long totalCommits = gitCommitRepository.count();
        if (totalCommits == 0) {
            return 0.0;
        }
        
        // Sample calculation: commits as percentage of expected contribution
        return Math.min(100.0, (totalCommits / 10.0)); // Normalize to 100%
    }
    
    private String determineHealthStatus(Double taskCompletion, Double commitContribution) {
        Double average = (taskCompletion + commitContribution) / 2;
        
        if (average >= 75) {
            return "EXCELLENT";
        } else if (average >= 50) {
            return "GOOD";
        } else if (average >= 25) {
            return "FAIR";
        } else {
            return "NEEDS_ATTENTION";
        }
    }
}
