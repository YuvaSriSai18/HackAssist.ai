package com.hackassist.ai.Service;

import com.hackassist.ai.models.GitHubRepository;
import com.hackassist.ai.models.GitCommit;
import com.hackassist.ai.models.User;
import java.util.List;
import java.util.Map;

public interface IGitHubService {
    
    List<GitHubRepository> fetchUserRepositories(String userToken);

    List<Map<String, Object>> fetchUserRepositoriesByUid(String uid);

    List<Map<String, Object>> fetchCommitsByUid(String uid, String owner, String repo);

    User getUserByUid(String uid);
    
    GitHubRepository saveRepository(GitHubRepository repository);
    
    List<GitCommit> fetchRepositoryCommits(Long repositoryId);
    
    void mapCommitsToTasks(Long projectId);
    
    List<GitCommit> getCommitsByRepositoryId(Long repositoryId);
}
