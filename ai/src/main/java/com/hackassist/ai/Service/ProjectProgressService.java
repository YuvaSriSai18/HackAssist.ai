package com.hackassist.ai.Service;

import com.hackassist.ai.dto.ProjectProgressResponse;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.TaskEvaluation;
import com.hackassist.ai.models.ProjectTask;
import com.hackassist.ai.models.ProjectEvaluationState;
import com.hackassist.ai.repository.ProjectEvaluationStateRepository;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.TaskEvaluationRepository;
import com.hackassist.ai.repository.ProjectTaskRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProjectProgressService {

    private static final int STUCK_TODO_HOURS = 24;
    private static final int NO_COMMITS_HOURS = 6;

    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final TaskEvaluationRepository taskEvaluationRepository;
    private final ProjectEvaluationStateRepository evaluationStateRepository;

    public ProjectProgressService(
        ProjectRepository projectRepository,
        ProjectTaskRepository projectTaskRepository,
        TaskEvaluationRepository taskEvaluationRepository,
        ProjectEvaluationStateRepository evaluationStateRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.taskEvaluationRepository = taskEvaluationRepository;
        this.evaluationStateRepository = evaluationStateRepository;
    }

    public ProjectProgressResponse getProjectProgress(Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        List<ProjectTask> tasks = projectTaskRepository.findByProject(project);
        List<TaskEvaluation> evaluations = taskEvaluationRepository.findByProjectIdOrderByLastEvaluatedAtDesc(projectId);

        double overallProgress = calculateOverallProgress(tasks, evaluations);
        List<ProjectProgressResponse.TaskProgressItem> taskItems = buildTaskItems(tasks, evaluations);
        List<ProjectProgressResponse.ActivityItem> activityItems = buildRecentActivity(evaluations);
        List<String> risks = detectRisks(projectId, tasks);

        return new ProjectProgressResponse(overallProgress, taskItems, activityItems, risks);
    }

    private double calculateOverallProgress(List<ProjectTask> tasks, List<TaskEvaluation> evaluations) {
        if (tasks.isEmpty()) {
            return 0.0;
        }
        int total = 0;
        for (ProjectTask task : tasks) {
            int progress = task.getProgress() == null ? 0 : task.getProgress();
            total += progress;
        }
        return total / (double) tasks.size();
    }

    private List<ProjectProgressResponse.TaskProgressItem> buildTaskItems(List<ProjectTask> tasks, List<TaskEvaluation> evaluations) {
        return tasks.stream()
            .map((task) -> new ProjectProgressResponse.TaskProgressItem(
                task.getId(),
                task.getProgress() == null ? 0 : task.getProgress(),
                task.getStatus() == null ? "TODO" : task.getStatus()
            ))
            .collect(Collectors.toList());
    }

    private List<ProjectProgressResponse.ActivityItem> buildRecentActivity(List<TaskEvaluation> evaluations) {
        return evaluations.stream()
            .limit(10)
            .map((evaluation) -> new ProjectProgressResponse.ActivityItem(
                evaluation.getTaskId(),
                evaluation.getProgress(),
                evaluation.getLastEvaluatedCommitSha(),
                evaluation.getLastEvaluatedAt() == null ? null : evaluation.getLastEvaluatedAt().toString()
            ))
            .collect(Collectors.toList());
    }

    private List<String> detectRisks(Long projectId, List<ProjectTask> tasks) {
        List<String> risks = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        Optional<ProjectEvaluationState> evaluationState = evaluationStateRepository.findByProjectId(projectId);
        if (evaluationState.isEmpty() || evaluationState.get().getLastEvaluatedAt() == null) {
            risks.add("No evaluation history found for project");
        } else {
            Duration sinceEval = Duration.between(evaluationState.get().getLastEvaluatedAt(), now);
            if (sinceEval.toHours() >= NO_COMMITS_HOURS) {
                risks.add("No commits detected in the last " + NO_COMMITS_HOURS + " hours");
            }
        }

        boolean stuckTodo = tasks.stream().anyMatch((task) -> {
            int progress = task.getProgress() == null ? 0 : task.getProgress();
            if (progress != 0) {
                return false;
            }
            LocalDateTime updated = task.getUpdatedAt();
            return updated != null && Duration.between(updated, now).toHours() >= STUCK_TODO_HOURS;
        });
        if (stuckTodo) {
            risks.add("Some tasks appear stuck in TODO");
        }

        long blockedCount = tasks.stream().filter((task) -> "BLOCKED".equalsIgnoreCase(task.getStatus())).count();
        if (blockedCount > 0) {
            risks.add("There are " + blockedCount + " blocked tasks");
        }

        return risks;
    }
}
