package com.hackassist.ai.Service;

import com.hackassist.ai.models.GitHubRepository;
import com.hackassist.ai.models.GitCommit;
import java.util.List;

public interface IGitHubService {
    
    List<GitHubRepository> fetchUserRepositories(String userToken);
    
    GitHubRepository saveRepository(GitHubRepository repository);
    
    List<GitCommit> fetchRepositoryCommits(Long repositoryId);
    
    void mapCommitsToTasks(Long projectId);
    
    List<GitCommit> getCommitsByRepositoryId(Long repositoryId);
}
