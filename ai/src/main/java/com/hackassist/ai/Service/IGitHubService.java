package com.hackassist.ai.Service;

import com.hackassist.ai.models.GitHubRepository;
import com.hackassist.ai.models.GitCommit;
import java.util.Map;
import java.util.List;

public interface IGitHubService {
    
    List<GitHubRepository> fetchUserRepositories(String userToken);

    List<Map<String, Object>> fetchUserRepositoriesFromGithub(String accessToken);
    
    GitHubRepository saveRepository(GitHubRepository repository);
    
    List<GitCommit> fetchRepositoryCommits(Long repositoryId);
    
    void mapCommitsToTasks(Long projectId);
    
    List<GitCommit> getCommitsByRepositoryId(Long repositoryId);
}
