package com.hackassist.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.hackassist.ai.models.GitHubRepository;
import com.hackassist.ai.models.GitCommit;
import com.hackassist.ai.Service.IGitHubService;
import com.hackassist.ai.dto.RepositoryDTO;
import com.hackassist.ai.dto.CommitDTO;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/github")
@CrossOrigin(origins = "*")
@Slf4j
public class GitHubController {
    
    @Autowired
    private IGitHubService githubService;
    
    @GetMapping("/repos")
    public ResponseEntity<?> getUserRepositories(@RequestParam String userToken) {
        try {
            List<GitHubRepository> repos = githubService.fetchUserRepositories(userToken);
            return ResponseEntity.ok(repos);
        } catch (Exception e) {
            log.error("Error fetching repositories", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch repositories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/commits/{repositoryId}")
    public ResponseEntity<?> getRepositoryCommits(@PathVariable Long repositoryId) {
        try {
            List<GitCommit> commits = githubService.fetchRepositoryCommits(repositoryId);
            return ResponseEntity.ok(commits);
        } catch (Exception e) {
            log.error("Error fetching commits", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch commits: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/map-commits")
    public ResponseEntity<?> mapCommitsToTasks(@RequestBody Map<String, Long> request) {
        try {
            Long projectId = request.get("projectId");
            githubService.mapCommitsToTasks(projectId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Commits mapped to tasks successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error mapping commits", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to map commits: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/repository/{repositoryId}/commits")
    public ResponseEntity<?> getCommitsByRepository(@PathVariable Long repositoryId) {
        try {
            List<GitCommit> commits = githubService.getCommitsByRepositoryId(repositoryId);
            return ResponseEntity.ok(commits);
        } catch (Exception e) {
            log.error("Error fetching commits by repository", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch commits: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
