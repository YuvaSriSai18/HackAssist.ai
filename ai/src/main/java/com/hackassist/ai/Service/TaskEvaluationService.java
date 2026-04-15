package com.hackassist.ai.Service;

import com.hackassist.ai.models.ProjectTask;
import com.hackassist.ai.models.evaluation.EvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskEvaluationService {

    private static final String PROMPT_TEMPLATE =
        "You are a senior engineer.\n\n" +
        "Task:\n%s\n\n" +
        "Repository Summary:\n%s\n\n" +
        "Evaluate task completion.\n\n" +
        "Return JSON:\n" +
        "{\n" +
        "  \"status\": \"TODO | IN_PROGRESS | COMPLETED\",\n" +
        "  \"completionPercentage\": number,\n" +
        "  \"confidence\": number\n" +
        "}";

    private final GeminiService geminiService;

    public TaskEvaluationService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public EvaluationResult evaluateTask(ProjectTask task, String repoSummary) {
        if (task == null) {
            throw new RuntimeException("Task is required for evaluation");
        }
        String taskText = task.getTitle() + "\n" + (task.getDescription() == null ? "" : task.getDescription());
        String prompt = String.format(PROMPT_TEMPLATE, taskText, repoSummary == null ? "" : repoSummary);

        EvaluationResult result = geminiService.generateJson(prompt, EvaluationResult.class);
        normalizeResult(result);
        log.info("Task evaluation complete: taskId={}, status={}, completion={}%, confidence={}",
            task.getId(), result.getStatus(), result.getCompletionPercentage(), result.getConfidence());
        return result;
    }

    public EvaluationResult evaluateTaskWithAI(ProjectTask task, String repoSummary) {
        return evaluateTask(task, repoSummary);
    }

    private void normalizeResult(EvaluationResult result) {
        if (result == null) {
            throw new RuntimeException("Evaluation result is null");
        }
        int completion = Math.max(0, Math.min(100, result.getCompletionPercentage()));
        double confidence = Math.max(0.0, Math.min(1.0, result.getConfidence()));
        result.setCompletionPercentage(completion);
        result.setConfidence(confidence);
        if (result.getStatus() == null || result.getStatus().isBlank()) {
            result.setStatus("TODO");
        }
    }
}
