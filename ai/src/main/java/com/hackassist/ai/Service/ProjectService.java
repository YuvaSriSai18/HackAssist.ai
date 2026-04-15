package com.hackassist.ai.Service;

import org.springframework.stereotype.Service;
import com.hackassist.ai.dto.ProjectRepoLinkRequest;
import com.hackassist.ai.dto.ProjectRequest;
import com.hackassist.ai.dto.ProjectResponse;
import com.hackassist.ai.dto.ProjectUpdateRequest;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.User;
import com.hackassist.ai.models.ProjectRepositoryMapping;
import com.hackassist.ai.repository.GitCommitRepository;
import com.hackassist.ai.repository.GitHubRepositoryRepository;
import com.hackassist.ai.repository.ProjectFeatureRepository;
import com.hackassist.ai.repository.ProjectModuleRepository;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.ProjectRiskRepository;
import com.hackassist.ai.repository.ProjectTaskRepository;
import com.hackassist.ai.repository.ProjectRepositoryMappingRepository;
import com.hackassist.ai.repository.RiskAlertRepository;
import com.hackassist.ai.repository.TaskDependencyRepository;
import com.hackassist.ai.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProjectService implements IProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final IGitHubService gitHubService;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectModuleRepository projectModuleRepository;
    private final ProjectFeatureRepository projectFeatureRepository;
    private final ProjectRiskRepository projectRiskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final RiskAlertRepository riskAlertRepository;
    private final GitHubRepositoryRepository gitHubRepositoryRepository;
    private final GitCommitRepository gitCommitRepository;
    private final ProjectRepositoryMappingRepository projectRepositoryMappingRepository;

    @Value("${app.webhook.callback-url:http://localhost:8080/webhook/github}")
    private String webhookCallbackUrl;

    public ProjectService(
        ProjectRepository projectRepository,
        UserRepository userRepository,
        IGitHubService gitHubService,
        ProjectTaskRepository projectTaskRepository,
        ProjectModuleRepository projectModuleRepository,
        ProjectFeatureRepository projectFeatureRepository,
        ProjectRiskRepository projectRiskRepository,
        TaskDependencyRepository taskDependencyRepository,
        RiskAlertRepository riskAlertRepository,
        GitHubRepositoryRepository gitHubRepositoryRepository,
        GitCommitRepository gitCommitRepository,
        ProjectRepositoryMappingRepository projectRepositoryMappingRepository
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.gitHubService = gitHubService;
        this.projectTaskRepository = projectTaskRepository;
        this.projectModuleRepository = projectModuleRepository;
        this.projectFeatureRepository = projectFeatureRepository;
        this.projectRiskRepository = projectRiskRepository;
        this.taskDependencyRepository = taskDependencyRepository;
        this.riskAlertRepository = riskAlertRepository;
        this.gitHubRepositoryRepository = gitHubRepositoryRepository;
        this.gitCommitRepository = gitCommitRepository;
        this.projectRepositoryMappingRepository = projectRepositoryMappingRepository;
    }

    @Override
    public ProjectResponse createProject(ProjectRequest request, String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCreatedBy(user);

        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    @Override
    public List<ProjectResponse> getProjectsByUser(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return projectRepository.findByCreatedByOrderByCreatedAtDesc(user).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public ProjectResponse updateProject(String projectId, ProjectUpdateRequest request, String userId) {
        Project project = getOwnedProject(projectId, userId);
        if (request.getName() != null && !request.getName().isBlank()) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProject(String projectId, String userId) {
        Project project = getOwnedProject(projectId, userId);

        taskDependencyRepository.deleteByProject(project);
        projectTaskRepository.deleteByProject(project);
        projectModuleRepository.deleteByProject(project);
        projectFeatureRepository.deleteByProject(project);
        projectRiskRepository.deleteByProject(project);
        riskAlertRepository.deleteByProject(project);

        gitHubRepositoryRepository.findByProject(project).forEach((repo) -> gitCommitRepository.deleteByRepository(repo));
        gitHubRepositoryRepository.deleteByProject(project);

        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public ProjectResponse linkRepository(String projectId, ProjectRepoLinkRequest request, String userId) {
        if (request == null || (isBlank(request.getRepoUrl()) && isBlank(request.getRepoFullName()))) {
            throw new RuntimeException("Repository details are required");
        }

        Project project = getOwnedProject(projectId, userId);

        // Prevent changing repository if already linked
        if (project.getGithubRepoLocked()) {
            throw new RuntimeException("Repository is already linked and cannot be changed");
        }

        if (!isRepoOwnedByUser(userId, request.getRepoUrl(), request.getRepoFullName())) {
            throw new RuntimeException("Repository is not accessible for this user");
        }

        String repoUrl = request.getRepoUrl();
        String repoFullName = request.getRepoFullName();
        
        if (isBlank(repoUrl) && !isBlank(repoFullName)) {
            repoUrl = "https://github.com/" + repoFullName;
        }

        // Extract owner and repo from URL or fullName
        String owner = null;
        String repo = null;
        
        if (!isBlank(repoFullName)) {
            String[] parts = repoFullName.split("/");
            if (parts.length == 2) {
                owner = parts[0];
                repo = parts[1];
            }
        } else if (!isBlank(repoUrl)) {
            // Parse from URL: https://github.com/owner/repo
            int lastSlash = repoUrl.lastIndexOf('/');
            if (lastSlash > 0) {
                repo = repoUrl.substring(lastSlash + 1);
                int secondLastSlash = repoUrl.lastIndexOf('/', lastSlash - 1);
                if (secondLastSlash > 0) {
                    owner = repoUrl.substring(secondLastSlash + 1, lastSlash);
                }
            }
        }

        if (isBlank(owner) || isBlank(repo)) {
            throw new RuntimeException("Could not parse repository owner and name");
        }

        // Create webhook via GitHub API (no secret - verification by repo mapping only)
        try {
            Long webhookId = gitHubService.createWebhook(userId, owner, repo, webhookCallbackUrl);
            
            // Store webhook metadata (no secrets stored)
            ProjectRepositoryMapping mapping = projectRepositoryMappingRepository
                .findByRepoFullName(owner + "/" + repo)
                .orElse(new ProjectRepositoryMapping(project.getId(), owner + "/" + repo));

            mapping.setProjectId(project.getId());
            mapping.setRepoFullName(owner + "/" + repo);
            mapping.setWebhookId(webhookId);
            mapping.setWebhookEnabled(true);
            mapping.setWebhookCreatedAt(LocalDateTime.now());
            
            projectRepositoryMappingRepository.save(mapping);
            
            // Lock the project so repository cannot be changed
            project.setGithubRepoUrl(repoUrl);
            project.setGithubRepoLocked(true);
            Project saved = projectRepository.save(project);
            
            log.info("Repository linked successfully for project {} with webhook ID {}", projectId, webhookId);
            return toResponse(saved);
            
        } catch (Exception ex) {
            log.error("Failed to create webhook for project {}: {}", projectId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to link repository: " + ex.getMessage());
        }
    }

    private Project getOwnedProject(String projectId, String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Project project = projectRepository.findByProjectIdAndCreatedBy(projectId, user);
        if (project == null) {
            throw new RuntimeException("Project not found or not owned by user");
        }
        return project;
    }

    private boolean isRepoOwnedByUser(String userId, String repoUrl, String repoFullName) {
        List<Map<String, Object>> repos = gitHubService.fetchUserRepositoriesByUid(userId);
        for (Map<String, Object> repo : repos) {
            String htmlUrl = repo.get("html_url") == null ? null : repo.get("html_url").toString();
            String fullName = repo.get("full_name") == null ? null : repo.get("full_name").toString();
            if (!isBlank(repoUrl) && repoUrl.equalsIgnoreCase(htmlUrl)) {
                return true;
            }
            if (!isBlank(repoFullName) && repoFullName.equalsIgnoreCase(fullName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
            project.getId(),
            project.getProjectId(),
            project.getName(),
            project.getDescription(),
            project.getCreatedAt(),
            project.getGithubRepoUrl()
        );
    }
}
