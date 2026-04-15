package com.hackassist.ai.Service;

import com.hackassist.ai.models.GitHubRepository;
import com.hackassist.ai.models.GitCommit;
import com.hackassist.ai.models.User;
import com.hackassist.ai.models.evaluation.FileChange;
import com.hackassist.ai.models.evaluation.CommitInfo;
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

    List<String> fetchRecentCommitShas(String uid, String owner, String repo, String sinceSha, int maxPages);

    List<FileChange> fetchChangedFilesForCommit(String uid, String owner, String repo, String commitSha);

    List<FileChange> getRecentFiles(Long projectId, int commitLookback);

    List<CommitInfo> getCommitsBetween(String uid, String owner, String repo, String fromSha, String toSha);

    List<FileChange> extractChangedFilesFromWebhookPayload(String uid, String owner, String repo, String latestSha, String payloadJson);

    Long createWebhook(String uid, String owner, String repo, String webhookUrl);

    void deleteWebhook(String uid, String owner, String repo, Long webhookId);
}
