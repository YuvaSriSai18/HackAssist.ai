package com.hackassist.ai.Service;

import org.springframework.stereotype.Service;
import com.hackassist.ai.dto.ProjectRepoLinkRequest;
import com.hackassist.ai.dto.ProjectRequest;
import com.hackassist.ai.dto.ProjectResponse;
import com.hackassist.ai.dto.ProjectUpdateRequest;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.User;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectService implements IProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final IGitHubService gitHubService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, IGitHubService gitHubService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.gitHubService = gitHubService;
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
    public void deleteProject(String projectId, String userId) {
        Project project = getOwnedProject(projectId, userId);
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
