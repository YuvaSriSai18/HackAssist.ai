package com.hackassist.ai.Service;

import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.ProjectEvaluationState;
import com.hackassist.ai.models.ProjectTask;
import com.hackassist.ai.models.TaskEvaluation;
import com.hackassist.ai.models.evaluation.FileChange;
import com.hackassist.ai.models.evaluation.FileSummary;
import com.hackassist.ai.models.evaluation.EvaluationResult;
import com.hackassist.ai.repository.TaskEvaluationRepository;
import com.hackassist.ai.repository.ProjectEvaluationStateRepository;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.ProjectTaskRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TaskEvaluationOrchestrator {

    private static final int RECENT_COMMITS_FOR_SUMMARY = 10;
    private static final int MAX_FILES_TO_SUMMARIZE = 50;

    private final IGitHubService gitHubService;
    private final FileSummarizationService fileSummarizationService;
    private final RepoSummaryService repoSummaryService;
    private final TaskEvaluationService taskEvaluationService;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectEvaluationStateRepository evaluationStateRepository;
    private final TaskEvaluationRepository taskEvaluationRepository;
    private final GeminiService geminiService;

    private static final boolean ENABLE_AI_EVAL = true;
    private static final boolean ENABLE_AI_SUMMARY = true;
    private static final int MIN_PROGRESS_DELTA = 5;
    private static final int MAX_PROGRESS_DELTA = 20;

    public TaskEvaluationOrchestrator(
        IGitHubService gitHubService,
        FileSummarizationService fileSummarizationService,
        RepoSummaryService repoSummaryService,
        TaskEvaluationService taskEvaluationService,
        ProjectRepository projectRepository,
        ProjectTaskRepository projectTaskRepository,
        ProjectEvaluationStateRepository evaluationStateRepository,
        TaskEvaluationRepository taskEvaluationRepository,
        GeminiService geminiService
    ) {
        this.gitHubService = gitHubService;
        this.fileSummarizationService = fileSummarizationService;
        this.repoSummaryService = repoSummaryService;
        this.taskEvaluationService = taskEvaluationService;
        this.projectRepository = projectRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.evaluationStateRepository = evaluationStateRepository;
        this.taskEvaluationRepository = taskEvaluationRepository;
        this.geminiService = geminiService;
    }

    @Transactional
    public void evaluateProject(Long projectId) {
        evaluateInternal(projectId, null, null, false);
    }

    @Async
    @Transactional
    public void evaluateProject(Long projectId, String latestSha) {
        evaluateInternal(projectId, latestSha, null, false);
    }

    @Async
    @Transactional
    public void evaluateProject(Long projectId, String latestSha, List<FileChange> webhookChanges, boolean forceReevaluate) {
        evaluateInternal(projectId, latestSha, webhookChanges, forceReevaluate);
    }

    private void evaluateInternal(Long projectId, String latestSha, List<FileChange> webhookChanges, boolean forceReevaluate) {
        int webhookFileCount = webhookChanges == null ? 0 : webhookChanges.size();
        log.info("Task evaluation started: projectId={}, latestSha={}, webhookFiles={}, forceReevaluate={}",
            projectId, latestSha, webhookFileCount, forceReevaluate);

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        String repoUrl = project.getGithubRepoUrl();
        if (repoUrl == null || repoUrl.isBlank()) {
            log.warn("Project {} has no GitHub repository linked", projectId);
            return;
        }

        RepoRef repoRef = parseRepoRef(repoUrl);
        if (repoRef == null) {
            throw new RuntimeException("Invalid GitHub repository URL");
        }

        ProjectEvaluationState state = evaluationStateRepository.findByProject(project)
            .orElseGet(() -> new ProjectEvaluationState(project));

        String evaluationSha = latestSha != null && !latestSha.isBlank()
            ? latestSha
            : state.getLastEvaluatedCommitSha();

        List<FileChange> recentFiles = gitHubService.getRecentFiles(projectId, RECENT_COMMITS_FOR_SUMMARY);
        log.info("Recent files fetched: count={} (commit lookback={})", recentFiles.size(), RECENT_COMMITS_FOR_SUMMARY);
        if (webhookChanges != null && !webhookChanges.isEmpty()) {
            recentFiles = mergeFileChanges(webhookChanges, recentFiles);
        }

        log.info("Recent files after merge: count={}", recentFiles.size());

        if (recentFiles.isEmpty()) {
            log.info("No recent files found for project {}", projectId);
            updateEvaluationState(state, evaluationSha);
            return;
        }

        Map<String, String> fileContents = new LinkedHashMap<>();
        for (FileChange change : recentFiles) {
            if (fileContents.size() >= MAX_FILES_TO_SUMMARIZE) {
                break;
            }
            fileContents.putIfAbsent(change.getFilename(), change.getContent());
        }

        log.info("File contents prepared for summarization: count={}", fileContents.size());

        if (fileContents.isEmpty()) {
            log.info("No changed files found for project {}", projectId);
            updateEvaluationState(state, evaluationSha);
            return;
        }

        String repoSummary = "";
        if (ENABLE_AI_EVAL || ENABLE_AI_SUMMARY) {
            try {
                List<FileChange> summaryInputs = fileContents.entrySet().stream()
                    .map((entry) -> new FileChange(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
                List<FileSummary> summaries = fileSummarizationService.summarizeFiles(summaryInputs);
                repoSummary = repoSummaryService.buildRepoSummary(summaries);
                log.debug("Repo summary built: filesSummarized={}, summaryLength={}",
                    summaries.size(), repoSummary == null ? 0 : repoSummary.length());
                if (repoSummary == null || repoSummary.isBlank()) {
                    log.warn("Repo summary is empty for project {}", projectId);
                    if (ENABLE_AI_EVAL) {
                        log.warn("AI evaluation disabled for project {} due to empty summary", projectId);
                    }
                }
            } catch (RuntimeException ex) {
                log.warn("AI summarization failed for project {}: {}", projectId, ex.getMessage());
                repoSummary = "";
            }
        }

        List<ProjectTask> tasks = projectTaskRepository.findByProject(project);

        if (tasks.isEmpty()) {
            log.info("No tasks found for project {}", projectId);
            updateEvaluationState(state, evaluationSha);
            return;
        }

        log.info("Evaluating {} tasks for project {}", tasks.size(), projectId);
        Set<Long> fallbackTaskIds = new HashSet<>();

        if (ENABLE_AI_EVAL && repoSummary != null && !repoSummary.isBlank()) {
            log.info("Starting AI evaluation for {} tasks (project: {})", tasks.size(), projectId);
            Map<Long, TaskEvaluation> evaluationMap = loadEvaluations(projectId, tasks);
            int aiSuccessCount = 0;
            for (ProjectTask task : tasks) {
                try {
                    log.debug("Evaluating task {} with AI: {}", task.getId(), task.getTitle());
                    EvaluationResult aiResult = taskEvaluationService.evaluateTaskWithAI(task, repoSummary);
                    int progress = aiResult.getCompletionPercentage();
                    TaskEvaluation evaluation = evaluationMap.computeIfAbsent(
                        task.getId(),
                        (id) -> new TaskEvaluation(task.getId(), projectId)
                    );
                    evaluation.setProgress(progress);
                    evaluation.setLastEvaluatedCommitSha(evaluationSha);
                    evaluation.setConfidenceScore(aiResult.getConfidence());
                    taskEvaluationRepository.save(evaluation);

                    log.trace("Task evaluation saved: {} (progress: {}%)", evaluation.getId(), progress);

                    updateTaskProgress(task, progress);
                    projectTaskRepository.save(task);
                    aiSuccessCount++;

                    log.info("Task {} AI evaluation complete: progress={}%, status={}",
                        task.getId(), progress, task.getStatus());
                } catch (RuntimeException ex) {
                    log.warn("AI evaluation failed for task {}: {}", task.getId(), ex.getMessage());
                    fallbackTaskIds.add(task.getId());
                }
            }
            log.info("AI evaluation completed: {} tasks succeeded, {} fallback", aiSuccessCount, fallbackTaskIds.size());
        }

        if (repoSummary != null && !repoSummary.isBlank() && (!ENABLE_AI_EVAL || !fallbackTaskIds.isEmpty())) {
            log.info("Starting summary-based evaluation for {} tasks (project: {})", tasks.size(), projectId);
            Map<Long, TaskEvaluation> evaluationMap = loadEvaluations(projectId, tasks);
            int summarySuccessCount = 0;

            for (ProjectTask task : tasks) {
                if (!fallbackTaskIds.isEmpty() && !fallbackTaskIds.contains(task.getId())) {
                    continue;
                }
                int inferredProgress = inferProgressFromSummary(task, repoSummary);
                TaskEvaluation evaluation = evaluationMap.computeIfAbsent(
                    task.getId(),
                    (id) -> new TaskEvaluation(task.getId(), projectId)
                );

                int currentProgress = evaluation.getProgress() == null ? 0 : evaluation.getProgress();
                int newProgress = Math.max(currentProgress, inferredProgress);
                evaluation.setProgress(newProgress);
                evaluation.setLastEvaluatedCommitSha(evaluationSha);
                evaluation.setConfidenceScore(0.4);
                taskEvaluationRepository.save(evaluation);

                updateTaskProgress(task, newProgress);
                projectTaskRepository.save(task);
                summarySuccessCount++;

                log.info("Task {} summary evaluation complete: progress={}%, status={}",
                    task.getId(), newProgress, task.getStatus());
            }

            log.info("Summary-based evaluation completed: {} updates applied", summarySuccessCount);
        }

        updateEvaluationState(state, evaluationSha);
        log.info("Task evaluation completed: projectId={}, evaluatedSha={}", projectId, evaluationSha);
    }

    private void updateEvaluationState(ProjectEvaluationState state, String latestSha) {
        state.setLastEvaluatedCommitSha(latestSha);
        state.setLastEvaluatedAt(LocalDateTime.now());
        evaluationStateRepository.save(state);
        Long projectId = state.getProject() == null ? null : state.getProject().getId();
        log.debug("Evaluation state updated: projectId={}, lastSha={}, evaluatedAt={}",
            projectId, latestSha, state.getLastEvaluatedAt());
    }

    private RepoRef parseRepoRef(String repoUrl) {
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

    private Map<Long, TaskEvaluation> loadEvaluations(Long projectId, List<ProjectTask> tasks) {
        Map<Long, TaskEvaluation> map = new LinkedHashMap<>();
        for (TaskEvaluation evaluation : taskEvaluationRepository.findByProjectIdOrderByLastEvaluatedAtDesc(projectId)) {
            map.put(evaluation.getTaskId(), evaluation);
        }
        for (ProjectTask task : tasks) {
            map.putIfAbsent(task.getId(), new TaskEvaluation(task.getId(), projectId));
        }
        return map;
    }

    private List<FileChange> mergeFileChanges(List<FileChange> primary, List<FileChange> secondary) {
        Map<String, String> merged = new LinkedHashMap<>();
        for (FileChange change : primary) {
            merged.put(change.getFilename(), change.getContent());
        }
        for (FileChange change : secondary) {
            merged.putIfAbsent(change.getFilename(), change.getContent());
        }
        log.debug("Merged webhook files: primary={}, secondary={}, merged={}",
            primary.size(), secondary.size(), merged.size());
        return merged.entrySet().stream()
            .map((entry) -> new FileChange(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    private int inferProgressFromSummary(ProjectTask task, String repoSummary) {
        if (repoSummary == null || repoSummary.isBlank()) {
            return 0;
        }
        String summary = repoSummary.toLowerCase();
        Set<String> keywordSet = new HashSet<>();
        keywordSet.addAll(extractKeywords(task.getTitle()));
        keywordSet.addAll(extractKeywords(task.getDescription()));
        boolean hasModuleHint = false;
        boolean moduleMatched = false;
        if (task.getDescription() != null && task.getDescription().toLowerCase().contains("module:")) {
            hasModuleHint = true;
            String moduleName = extractModuleName(task.getDescription().toLowerCase());
            if (!moduleName.isBlank() && summary.contains(moduleName)) {
                moduleMatched = true;
            }
        }

        int total = keywordSet.size() + (hasModuleHint ? 1 : 0);
        if (total == 0) {
            return 0;
        }

        int matched = moduleMatched ? 1 : 0;
        for (String keyword : keywordSet) {
            if (summary.contains(keyword)) {
                matched++;
            }
        }

        double ratio = total == 0 ? 0.0 : (double) matched / total;
        log.debug("Summary match ratio for task {}: matched={}, total={}, ratio={}",
            task.getId(), matched, total, ratio);
        if (ratio >= 0.6) {
            return 80;
        }
        if (ratio >= 0.3) {
            return 50;
        }
        if (ratio >= 0.1) {
            return 30;
        }
        return 0;
    }



    private List<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.split("\\W+"))
            .filter((token) -> token.length() >= 4)
            .map(String::toLowerCase)
            .collect(Collectors.toList());
    }

    private String extractModuleName(String description) {
        int idx = description.indexOf("module:");
        if (idx < 0) {
            return "";
        }
        String tail = description.substring(idx + 7).trim();
        int spaceIdx = tail.indexOf(' ');
        return spaceIdx > 0 ? tail.substring(0, spaceIdx).toLowerCase() : tail.toLowerCase();
    }



    private void updateTaskProgress(ProjectTask task, int progress) {
        if (task == null) {
            log.warn("Cannot update progress: task is null");
            return;
        }

        // Store progress percentage
        task.setProgress(progress);
        
        // Map progress to status (NOT using confidence)
        String newStatus;
        if (progress >= 90) {
            newStatus = "COMPLETED";
            task.setCompletedAt(LocalDateTime.now());
            log.info("Task {} progress {}% → Status: COMPLETED (marked completed at {})", 
                task.getId(), progress, task.getCompletedAt());
        } else if (progress >= 30) {
            newStatus = "IN_PROGRESS";
            log.info("Task {} progress {}% → Status: IN_PROGRESS", task.getId(), progress);
        } else {
            newStatus = "TODO";
            log.info("Task {} progress {}% → Status: TODO", task.getId(), progress);
        }
        
        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());
        
        log.debug("Task {} updated: progress={}, status={}, updatedAt={}", 
            task.getId(), progress, newStatus, task.getUpdatedAt());
    }


}
