package com.hackassist.ai.Service;

import org.springframework.stereotype.Service;
import com.hackassist.ai.dto.ProjectRepoLinkRequest;
import com.hackassist.ai.dto.ProjectRequest;
import com.hackassist.ai.dto.ProjectResponse;
import com.hackassist.ai.dto.ProjectUpdateRequest;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.User;
import com.hackassist.ai.repository.GitCommitRepository;
import com.hackassist.ai.repository.GitHubRepositoryRepository;
import com.hackassist.ai.repository.ProjectFeatureRepository;
import com.hackassist.ai.repository.ProjectModuleRepository;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.ProjectRiskRepository;
import com.hackassist.ai.repository.ProjectTaskRepository;
import com.hackassist.ai.repository.RiskAlertRepository;
import com.hackassist.ai.repository.TaskDependencyRepository;
import com.hackassist.ai.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
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
        GitCommitRepository gitCommitRepository
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
    public ProjectResponse linkRepository(String projectId, ProjectRepoLinkRequest request, String userId) {
        if (request == null || (isBlank(request.getRepoUrl()) && isBlank(request.getRepoFullName()))) {
            throw new RuntimeException("Repository details are required");
        }

        Project project = getOwnedProject(projectId, userId);
        if (!isRepoOwnedByUser(userId, request.getRepoUrl(), request.getRepoFullName())) {
            throw new RuntimeException("Repository is not accessible for this user");
        }

        String repoUrl = request.getRepoUrl();
        if (isBlank(repoUrl) && !isBlank(request.getRepoFullName())) {
            repoUrl = "https://github.com/" + request.getRepoFullName();
        }

        project.setGithubRepoUrl(repoUrl);
        Project saved = projectRepository.save(project);
        return toResponse(saved);
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
