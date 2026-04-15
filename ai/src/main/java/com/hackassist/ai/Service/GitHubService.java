package com.hackassist.ai.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hackassist.ai.models.GitHubRepository;
import com.hackassist.ai.models.GitCommit;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.ProjectTask;
import com.hackassist.ai.models.User;
import com.hackassist.ai.models.evaluation.FileChange;
import com.hackassist.ai.models.evaluation.CommitInfo;
import com.hackassist.ai.repository.GitHubRepositoryRepository;
import com.hackassist.ai.repository.GitCommitRepository;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.ProjectTaskRepository;
import com.hackassist.ai.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class GitHubService implements IGitHubService {

    private static final int MAX_FILE_SIZE_BYTES = 200_000;
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MAX_FILES_FROM_WEBHOOK = 50;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private GitHubRepositoryRepository githubRepositoryRepository;
    
    @Autowired
    private GitCommitRepository gitCommitRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ProjectTaskRepository projectTaskRepository;

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
        
        List<ProjectTask> allTasks = projectTaskRepository.findAll();
        List<GitCommit> allCommits = gitCommitRepository.findAll();
        
        // Simple keyword matching strategy
        for (GitCommit commit : allCommits) {
            for (ProjectTask task : allTasks) {
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

    @Override
    public List<String> fetchRecentCommitShas(String uid, String owner, String repo, String sinceSha, int maxPages) {
        String accessToken = getAccessTokenForUid(uid);
        List<String> result = new ArrayList<>();

        int page = 1;
        int pagesToFetch = Math.max(1, maxPages);
        while (page <= pagesToFetch) {
            String url = "https://api.github.com/repos/" + owner + "/" + repo
                + "/commits?per_page=" + DEFAULT_PAGE_SIZE + "&page=" + page;
            List<Map<String, Object>> commits = getFromGitHub(url, accessToken);
            if (commits.isEmpty()) {
                break;
            }

            boolean foundSinceSha = false;
            for (Map<String, Object> commit : commits) {
                String sha = commit.get("sha") == null ? null : commit.get("sha").toString();
                if (sha == null) {
                    continue;
                }
                if (sinceSha != null && sinceSha.equalsIgnoreCase(sha)) {
                    foundSinceSha = true;
                    break;
                }
                result.add(sha);
            }

            if (foundSinceSha) {
                break;
            }
            page++;
        }

        return result;
    }

    @Override
    public List<FileChange> fetchChangedFilesForCommit(String uid, String owner, String repo, String commitSha) {
        String accessToken = getAccessTokenForUid(uid);
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/commits/" + commitSha;
        String responseBody = getRawFromGitHub(url, accessToken);

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode files = root.path("files");
            if (!files.isArray()) {
                return List.of();
            }

            List<FileChange> changes = new ArrayList<>();
            for (JsonNode file : files) {
                String filename = file.path("filename").asText(null);
                long size = file.path("size").asLong(0);

                if (filename == null || filename.isBlank()) {
                    continue;
                }
                if (size > MAX_FILE_SIZE_BYTES) {
                    log.debug("Skipping large file {} ({} bytes)", filename, size);
                    continue;
                }

                String content = fetchFileContentAtSha(owner, repo, commitSha, filename, accessToken);
                if (content == null || content.isBlank()) {
                    continue;
                }

                changes.add(new FileChange(filename, content));
            }

            return changes;
        } catch (Exception ex) {
            log.error("Failed to parse commit details for {}: {}", commitSha, ex.getMessage(), ex);
            throw new RuntimeException("Failed to parse commit details for " + commitSha);
        }
    }

    @Override
    public List<FileChange> getRecentFiles(Long projectId, int commitLookback) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        String repoUrl = project.getGithubRepoUrl();
        if (repoUrl == null || repoUrl.isBlank()) {
            log.warn("Project {} has no GitHub repository linked", projectId);
            return List.of();
        }

        RepoRef repoRef = parseRepoRef(repoUrl);
        if (repoRef == null) {
            throw new RuntimeException("Invalid GitHub repository URL");
        }

        String uid = project.getCreatedBy().getUid();
        int maxCommits = Math.max(1, commitLookback);
        List<String> commitShas = fetchRecentCommitShas(uid, repoRef.owner, repoRef.repo, null, maxCommits);

        if (commitShas.isEmpty()) {
            return List.of();
        }

        Map<String, String> fileContents = new LinkedHashMap<>();
        for (String sha : commitShas) {
            List<FileChange> changes = fetchChangedFilesForCommit(uid, repoRef.owner, repoRef.repo, sha);
            for (FileChange change : changes) {
                if (fileContents.size() >= MAX_FILES_FROM_WEBHOOK) {
                    break;
                }
                fileContents.putIfAbsent(change.getFilename(), change.getContent());
            }
            if (fileContents.size() >= MAX_FILES_FROM_WEBHOOK) {
                break;
            }
        }

        return fileContents.entrySet().stream()
            .map((entry) -> new FileChange(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    public List<CommitInfo> getCommitsBetween(String uid, String owner, String repo, String fromSha, String toSha) {
        String accessToken = getAccessTokenForUid(uid);
        List<CommitInfo> result = new ArrayList<>();

        int page = 1;
        boolean collecting = (toSha == null || toSha.isBlank());
        boolean foundFrom = false;

        while (!foundFrom) {
            String url = "https://api.github.com/repos/" + owner + "/" + repo
                + "/commits?per_page=" + DEFAULT_PAGE_SIZE + "&page=" + page;
            List<Map<String, Object>> commits = getFromGitHub(url, accessToken);
            if (commits.isEmpty()) {
                break;
            }

            for (Map<String, Object> commit : commits) {
                String sha = commit.get("sha") == null ? null : commit.get("sha").toString();
                String message = extractCommitMessage(commit);
                if (sha == null) {
                    continue;
                }

                if (!collecting && sha.equalsIgnoreCase(toSha)) {
                    collecting = true;
                }

                if (collecting) {
                    if (fromSha != null && sha.equalsIgnoreCase(fromSha)) {
                        foundFrom = true;
                        break;
                    }
                    result.add(new CommitInfo(sha, message));
                }
            }

            if (foundFrom || commits.size() < DEFAULT_PAGE_SIZE) {
                break;
            }
            page++;
        }

        if (!collecting && toSha != null) {
            return List.of();
        }

        return result;
    }

    @Override
    public List<FileChange> extractChangedFilesFromWebhookPayload(
        String uid,
        String owner,
        String repo,
        String latestSha,
        String payloadJson
    ) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return List.of();
        }
        String accessToken = getAccessTokenForUid(uid);
        LinkedHashSet<String> files = new LinkedHashSet<>();

        try {
            JsonNode root = objectMapper.readTree(payloadJson);
            JsonNode commits = root.path("commits");
            if (!commits.isArray()) {
                return List.of();
            }

            for (JsonNode commit : commits) {
                addFiles(files, commit.path("added"));
                addFiles(files, commit.path("modified"));
                if (files.size() >= MAX_FILES_FROM_WEBHOOK) {
                    break;
                }
            }

            List<FileChange> changes = new ArrayList<>();
            for (String path : files) {
                if (changes.size() >= MAX_FILES_FROM_WEBHOOK) {
                    break;
                }
                String content = fetchFileContentAtSha(owner, repo, latestSha, path, accessToken);
                if (content == null || content.isBlank()) {
                    continue;
                }
                if (content.length() > MAX_FILE_SIZE_BYTES) {
                    log.debug("Skipping large file from webhook: {}", path);
                    continue;
                }
                changes.add(new FileChange(path, content));
            }

            return changes;
        } catch (Exception ex) {
            log.error("Failed to parse webhook payload: {}", ex.getMessage(), ex);
            return List.of();
        }
    }
    
    private boolean isCommitRelatedToTask(GitCommit commit, ProjectTask task) {
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

    private String getRawFromGitHub(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            request,
            String.class
        );

        return response.getBody() == null ? "" : response.getBody();
    }

    private String fetchRawFileContent(String rawUrl, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<Void> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            rawUrl,
            HttpMethod.GET,
            request,
            String.class
        );

        return response.getBody();
    }

    private String fetchFileContentAtSha(String owner, String repo, String sha, String path, String accessToken) {
        if (owner == null || owner.isBlank() || repo == null || repo.isBlank()) {
            log.warn("Cannot fetch file: owner/repo missing (owner={}, repo={})", owner, repo);
            return null;
        }
        if (sha == null || sha.isBlank()) {
            log.warn("Cannot fetch file: commit sha missing for {}/{}", owner, repo);
            return null;
        }
        if (path == null || path.isBlank()) {
            log.warn("Cannot fetch file: path missing for {}/{}@{}", owner, repo, sha);
            return null;
        }

        String cleanPath = decodePath(path);
        String url = buildContentsUrl(owner, repo, cleanPath, sha);

        log.info("Fetching file from GitHub:");
        log.info("Repo: {}/{}", owner, repo);
        log.info("Commit: {}", sha);
        log.info("Path: {}", cleanPath);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> request = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                String.class
            );

            String bodyText = response.getBody();
            if (bodyText == null || bodyText.isBlank()) {
                log.warn("Empty GitHub response body for {}", cleanPath);
                return null;
            }

            JsonNode body = objectMapper.readTree(bodyText);
            if (body == null || body.path("content").isMissingNode()) {
                log.warn("No content field in GitHub response for {}", cleanPath);
                return null;
            }

            String encoding = body.path("encoding").asText("base64");
            if (!"base64".equalsIgnoreCase(encoding)) {
                log.warn("Unexpected encoding '{}' for {}", encoding, cleanPath);
                return null;
            }

            String base64 = body.path("content").asText("");
            if (base64.isBlank()) {
                return null;
            }

            String normalized = base64.replaceAll("\\s", "");
            return new String(Base64.getDecoder().decode(normalized), StandardCharsets.UTF_8);
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("GitHub returned 404 for {} (repo: {}/{}, sha: {})", cleanPath, owner, repo, sha);
            return null;
        } catch (HttpClientErrorException ex) {
            log.error("GitHub API error for {}: {}", cleanPath, ex.getMessage());
            return null;
        } catch (Exception ex) {
            log.error("Failed to fetch file content for {}: {}", cleanPath, ex.getMessage(), ex);
            return null;
        }
    }

    private String decodePath(String path) {
        try {
            return URLDecoder.decode(path, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            log.debug("Failed to decode path {}, using original", path);
            return path;
        }
    }

    private String buildContentsUrl(String owner, String repo, String path, String sha) {
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        String[] segments = normalized.split("/");
        String encodedPath = Arrays.stream(segments)
            .filter((segment) -> !segment.isBlank())
            .map((segment) -> URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"))
            .collect(Collectors.joining("/"));

        return "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + encodedPath + "?ref=" + sha;
    }

    private void addFiles(LinkedHashSet<String> files, JsonNode items) {
        if (!items.isArray()) {
            return;
        }
        for (JsonNode item : items) {
            String value = item.asText(null);
            if (value != null && !value.isBlank()) {
                files.add(value);
            }
        }
    }

    private String extractCommitMessage(Map<String, Object> commit) {
        Object commitObj = commit.get("commit");
        if (!(commitObj instanceof Map<?, ?>)) {
            return "";
        }
        Object message = ((Map<?, ?>) commitObj).get("message");
        return message == null ? "" : message.toString();
    }

    private RepoRef parseRepoRef(String repoUrl) {
        if (repoUrl == null) {
            return null;
        }
        String cleaned = repoUrl.trim();
        if (cleaned.endsWith(".git")) {
            cleaned = cleaned.substring(0, cleaned.length() - 4);
        }
        String marker = "github.com/";
        int idx = cleaned.indexOf(marker);
        if (idx < 0) {
            return null;
        }
        String path = cleaned.substring(idx + marker.length());
        String[] parts = path.split("/");
        if (parts.length < 2) {
            return null;
        }
        return new RepoRef(parts[0], parts[1]);
    }

    private static class RepoRef {
        private final String owner;
        private final String repo;

        private RepoRef(String owner, String repo) {
            this.owner = owner;
            this.repo = repo;
        }
    }

    /**
     * Create a GitHub webhook for repository push events
     * @param uid User ID
     * @param owner Repository owner
     * @param repo Repository name
     * @param webhookUrl Callback URL where GitHub will send events
     * @return GitHub webhook ID
     */
    public Long createWebhook(String uid, String owner, String repo, String webhookUrl) {
        String accessToken = getAccessTokenForUid(uid);
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/hooks";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            // Create webhook payload (no secret - verification by repo mapping only)
            String payload = String.format(
                "{\"name\":\"web\"," +
                "\"active\":true," +
                "\"events\":[\"push\"]," +
                "\"config\":{" +
                "\"url\":\"%s\"," +
                "\"content_type\":\"json\"" +
                "}" +
                "}",
                webhookUrl
            );

            HttpEntity<String> request = new HttpEntity<>(payload, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getBody() != null && response.getBody().containsKey("id")) {
                Long webhookId = Long.valueOf(response.getBody().get("id").toString());
                log.info("Webhook created successfully for {}/{} with ID: {}", owner, repo, webhookId);
                return webhookId;
            }

            throw new RuntimeException("Failed to create webhook: Invalid response from GitHub API");
        } catch (Exception ex) {
            log.error("Failed to create webhook for {}/{}: {}", owner, repo, ex.getMessage(), ex);
            throw new RuntimeException("Failed to create webhook: " + ex.getMessage());
        }
    }

    /**
     * Delete a GitHub webhook
     * @param uid User ID
     * @param owner Repository owner
     * @param repo Repository name
     * @param webhookId Webhook ID to delete
     */
    public void deleteWebhook(String uid, String owner, String repo, Long webhookId) {
        String accessToken = getAccessTokenForUid(uid);
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/hooks/" + webhookId;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Void> request = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                request,
                Void.class
            );

            log.info("Webhook deleted successfully for {}/{} with ID: {}", owner, repo, webhookId);
        } catch (Exception ex) {
            log.error("Failed to delete webhook for {}/{}: {}", owner, repo, ex.getMessage(), ex);
            throw new RuntimeException("Failed to delete webhook: " + ex.getMessage());
        }
    }
}
