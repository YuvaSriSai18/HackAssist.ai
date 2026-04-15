package com.hackassist.ai.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hackassist.ai.dto.ProjectSummaryDTO;
import com.hackassist.ai.dto.RiskAlertDTO;
import com.hackassist.ai.dto.TeamMemberActivityDTO;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.RiskAlert;
import com.hackassist.ai.models.RiskType;
import com.hackassist.ai.models.AlertStatus;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.RiskAlertRepository;
import com.hackassist.ai.repository.ProjectTaskRepository;
import com.hackassist.ai.repository.GitCommitRepository;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IntelligenceService implements IIntelligenceService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private RiskAlertRepository riskAlertRepository;
    
    @Autowired
    private ProjectTaskRepository projectTaskRepository;
    
    @Autowired
    private GitCommitRepository gitCommitRepository;
    
    @Autowired
    private IProgressService progressService;
    
    @Override
    public List<RiskAlertDTO> detectRisks(Long projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            throw new RuntimeException("Project not found");
        }
        
        List<RiskAlert> alerts = new ArrayList<>();
        
        // Check for no commits in the last 6 hours
        LocalDateTime sixHoursAgo = LocalDateTime.now().minusHours(6);
        long recentCommits = gitCommitRepository.findByCommitDateAfter(sixHoursAgo).size();
        if (recentCommits == 0) {
            RiskAlert alert = new RiskAlert();
            alert.setProject(project.get());
            alert.setRiskType(RiskType.NO_COMMITS_IN_HOURS);
            alert.setDescription("No commits detected in the last 6 hours");
            alert.setStatus(AlertStatus.ACTIVE);
            alerts.add(alert);
        }
        
        // Check for too many pending tasks
        long pendingCount = projectTaskRepository.findAll().stream()
            .filter(t -> {
                String status = t.getStatus();
                return status != null && ("TODO".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status));
            })
            .count();
        if (pendingCount > 10) {
            RiskAlert alert = new RiskAlert();
            alert.setProject(project.get());
            alert.setRiskType(RiskType.TOO_MANY_PENDING_TASKS);
            alert.setDescription("Too many pending tasks: " + pendingCount);
            alert.setStatus(AlertStatus.ACTIVE);
            alerts.add(alert);
        }
        
        // Save detected risks
        alerts.forEach(alert -> riskAlertRepository.save(alert));
        
        return alerts.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public ProjectSummaryDTO generateProjectSummaryForPresentation(Long projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            throw new RuntimeException("Project not found");
        }
        
        ProjectSummaryDTO summary = new ProjectSummaryDTO();
        summary.setProjectId(projectId);
        summary.setProjectName(project.get().getName());
        summary.setDescription(project.get().getDescription());
        summary.setStatus(project.get().getStatus().toString());
        
        Double completion = progressService.calculateTaskCompletionPercentage(projectId);
        summary.setCompletionPercentage(completion);
        
        List<RiskAlert> risks = riskAlertRepository.findByProject(project.get());
        summary.setActiveRisks(risks.stream().map(this::convertToDTO).collect(Collectors.toList()));
        
        return summary;
    }
    
    @Override
    public String suggestNextActions(Long projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            throw new RuntimeException("Project not found");
        }
        
        StringBuilder suggestions = new StringBuilder();
        
        // Analyze progress and suggest actions
        Double completion = progressService.calculateTaskCompletionPercentage(projectId);
        
        if (completion < 30) {
            suggestions.append("• Focus on completing core features first\n");
            suggestions.append("• Increase team communication and task assignments\n");
        } else if (completion < 70) {
            suggestions.append("• Prioritize remaining high-priority tasks\n");
            suggestions.append("• Begin testing and quality assurance\n");
        } else {
            suggestions.append("• Begin documentation and presentation preparation\n");
            suggestions.append("• Perform final testing and bug fixes\n");
        }
        
        return suggestions.toString();
    }
    
    private RiskAlertDTO convertToDTO(RiskAlert alert) {
        return new RiskAlertDTO(
            alert.getId(),
            alert.getProject().getId(),
            alert.getRiskType().toString(),
            alert.getDescription(),
            alert.getSeverity().toString(),
            alert.getDetectedAt(),
            alert.getResolvedAt(),
            alert.getStatus().toString(),
            alert.getSuggestedAction()
        );
    }
}
