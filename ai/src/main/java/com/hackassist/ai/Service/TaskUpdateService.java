package com.hackassist.ai.Service;

import com.hackassist.ai.models.ProjectTask;
import com.hackassist.ai.models.evaluation.EvaluationResult;
import com.hackassist.ai.repository.ProjectTaskRepository;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskUpdateService {

    private final ProjectTaskRepository projectTaskRepository;

    public TaskUpdateService(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    public ProjectTask applyEvaluation(ProjectTask task, EvaluationResult result) {
        if (task == null || result == null) {
            throw new RuntimeException("Task and evaluation result are required");
        }

        String status = mapStatus(result.getCompletionPercentage());
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());

        if ("COMPLETED".equals(status)) {
            task.setCompletedAt(LocalDateTime.now());
        }

        ProjectTask saved = projectTaskRepository.save(task);
        log.info("Task updated: taskId={}, status={}, completion={}%, confidence={}",
            saved.getId(), status, result.getCompletionPercentage(), result.getConfidence());
        return saved;
    }

    private String mapStatus(int completionPercentage) {
        if (completionPercentage >= 90) {
            return "COMPLETED";
        }
        if (completionPercentage >= 40) {
            return "IN_PROGRESS";
        }
        return "TODO";
    }
}
