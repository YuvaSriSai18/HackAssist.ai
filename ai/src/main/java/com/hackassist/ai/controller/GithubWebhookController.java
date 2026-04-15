package com.hackassist.ai.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackassist.ai.Service.GitHubService;
import com.hackassist.ai.Service.TaskEvaluationOrchestrator;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.ProjectRepositoryMapping;
import com.hackassist.ai.models.evaluation.FileChange;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.ProjectRepositoryMappingRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook/github")
@Slf4j
public class GithubWebhookController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProjectRepositoryMappingRepository mappingRepository;
    private final ProjectRepository projectRepository;
    private final GitHubService gitHubService;
    private final TaskEvaluationOrchestrator orchestrator;

    public GithubWebhookController(
        ProjectRepositoryMappingRepository mappingRepository,
        ProjectRepository projectRepository,
        GitHubService gitHubService,
        TaskEvaluationOrchestrator orchestrator
    ) {
        this.mappingRepository = mappingRepository;
        this.projectRepository = projectRepository;
        this.gitHubService = gitHubService;
        this.orchestrator = orchestrator;
    }

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
        @RequestHeader("X-GitHub-Event") String event,
        @RequestBody String payload,
        @RequestParam(value = "forceReevaluate", required = false, defaultValue = "false") boolean forceReevaluate
    ) {
        log.info("GitHub webhook received: event={}", event);

        // Only process push events
        if (!"push".equalsIgnoreCase(event)) {
            return ResponseEntity.ok().build();
        }

        JsonNode root = parsePayload(payload);
        String repoFullName = root.path("repository").path("full_name").asText(null);
        String latestSha = root.path("after").asText(null);

        if (repoFullName == null || latestSha == null) {
            log.warn("Webhook payload missing repo or sha");
            return ResponseEntity.ok().build();
        }

        // Verify webhook by checking if repo is registered in our database
        // Security: Only we can create webhooks via authenticated GitHub API,
        // so any webhook we receive for a registered repo is legitimate
        ProjectRepositoryMapping mapping = mappingRepository.findByRepoFullName(repoFullName).orElse(null);
        if (mapping == null) {
            log.warn("Webhook received for unregistered repo: {}", repoFullName);
            return ResponseEntity.status(401).build();
        }

        if (!mapping.getWebhookEnabled()) {
            log.warn("Webhook received but disabled for repo: {}", repoFullName);
            return ResponseEntity.status(401).build();
        }

        Project project = projectRepository.findById(mapping.getProjectId()).orElse(null);
        if (project == null) {
            log.warn("Mapped project not found: {}", mapping.getProjectId());
            return ResponseEntity.status(401).build();
        }

        String owner = root.path("repository").path("owner").path("name").asText(null);
        if (owner == null || owner.isBlank()) {
            owner = repoFullName.split("/")[0];
        }
        String repo = root.path("repository").path("name").asText(null);
        if (repo == null || repo.isBlank()) {
            repo = repoFullName.split("/")[1];
        }

        List<FileChange> changes = gitHubService.extractChangedFilesFromWebhookPayload(
            project.getCreatedBy().getUid(),
            owner,
            repo,
            latestSha,
            payload
        );

        log.info("Webhook mapped to project {}, repo {}, commits processed", project.getId(), repoFullName);
        orchestrator.evaluateProject(project.getId(), latestSha, changes, forceReevaluate);

        return ResponseEntity.accepted().build();
    }

    private JsonNode parsePayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new RuntimeException("Invalid webhook payload");
        }
    }
}
