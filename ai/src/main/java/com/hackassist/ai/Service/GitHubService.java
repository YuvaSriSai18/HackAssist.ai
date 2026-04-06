package com.hackassist.ai.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hackassist.ai.models.GitHubRepository;
import com.hackassist.ai.models.GitCommit;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.Tasks;
import com.hackassist.ai.models.User;
import com.hackassist.ai.repository.GitHubRepositoryRepository;
import com.hackassist.ai.repository.GitCommitRepository;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.TaskRepository;
import com.hackassist.ai.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class GitHubService implements IGitHubService {
    
    @Autowired
    private GitHubRepositoryRepository githubRepositoryRepository;
    
    @Autowired
    private GitCommitRepository gitCommitRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    
    @Override
    public List<GitHubRepository> fetchUserRepositories(String userToken) {
        log.info("Fetching repositories for user with token");
        // TODO: Consider mapping API response to GitHubRepository entities
        return githubRepositoryRepository.findAll();
    }

    @Override
    public List<Map<String, Object>> fetchUserRepositoriesByUid(String uid) {
        String accessToken = getAccessTokenForUid(uid);
        String url = "https://api.github.com/user/repos?visibility=all&affiliation=owner,collaborator,organization_member&per_page=100";
        return getFromGitHub(url, accessToken);
    }

    @Override
    public List<Map<String, Object>> fetchCommitsByUid(String uid, String owner, String repo) {
        String accessToken = getAccessTokenForUid(uid);
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/commits?per_page=100";
        return getFromGitHub(url, accessToken);
    }

    @Override
    public User getUserByUid(String uid) {
        return userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + uid));
    }
    
    @Override
    public GitHubRepository saveRepository(GitHubRepository repository) {
        if (repository.getUser() == null) {
            throw new RuntimeException("Repository must have an associated user");
        }
        return githubRepositoryRepository.save(repository);
    }
    
    @Override
    public List<GitCommit> fetchRepositoryCommits(Long repositoryId) {
        Optional<GitHubRepository> repository = githubRepositoryRepository.findById(repositoryId);
        if (!repository.isPresent()) {
            throw new RuntimeException("Repository not found with id: " + repositoryId);
        }
        
        log.info("Fetching commits for repository: {}", repository.get().getRepoName());
        // TODO: Implement actual GitHub API integration to fetch commits
        return gitCommitRepository.findByRepositoryOrderByCommitDateDesc(repository.get());
    }
    
    @Override
    public void mapCommitsToTasks(Long projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            throw new RuntimeException("Project not found");
        }
        
        log.info("Mapping commits to tasks for project: {}", projectId);
        
        List<Tasks> allTasks = taskRepository.findAll();
        List<GitCommit> allCommits = gitCommitRepository.findAll();
        
        // Simple keyword matching strategy
        for (GitCommit commit : allCommits) {
            for (Tasks task : allTasks) {
                if (isCommitRelatedToTask(commit, task)) {
                    commit.setMappedTask(task);
                    gitCommitRepository.save(commit);
                }
            }
        }
    }
    
    @Override
    public List<GitCommit> getCommitsByRepositoryId(Long repositoryId) {
        Optional<GitHubRepository> repository = githubRepositoryRepository.findById(repositoryId);
        if (!repository.isPresent()) {
            throw new RuntimeException("Repository not found");
        }
        return gitCommitRepository.findByRepositoryOrderByCommitDateDesc(repository.get());
    }
    
    private boolean isCommitRelatedToTask(GitCommit commit, Tasks task) {
        String commitMessage = commit.getMessage().toLowerCase();
        String taskTitle = task.getTitle().toLowerCase();
        
        // Simple keyword matching
        return commitMessage.contains(taskTitle) || 
               commitMessage.contains(task.getId().toString()) ||
               taskTitle.contains(extractKeyword(commitMessage));
    }
    
    private String extractKeyword(String text) {
        String[] words = text.split(" ");
        return words.length > 0 ? words[0] : "";
    }

    private String getAccessTokenForUid(String uid) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("github", uid);
        if (client == null || client.getAccessToken() == null) {
            throw new RuntimeException("GitHub access token not found for uid: " + uid);
        }
        return client.getAccessToken().getTokenValue();
    }

    private List<Map<String, Object>> getFromGitHub(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        return response.getBody() == null ? List.of() : response.getBody();
    }
}
