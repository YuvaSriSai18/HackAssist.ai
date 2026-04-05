package com.hackassist.ai.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hackassist.ai.models.GitHubRepository;
import com.hackassist.ai.models.GitCommit;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.Tasks;
import com.hackassist.ai.repository.GitHubRepositoryRepository;
import com.hackassist.ai.repository.GitCommitRepository;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    
    @Override
    public List<GitHubRepository> fetchUserRepositories(String userToken) {
        log.info("Fetching repositories for user with token");
        // TODO: Implement actual GitHub API integration with OAuth token
        return githubRepositoryRepository.findAll();
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
}
