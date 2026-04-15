package com.hackassist.ai.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hackassist.ai.models.evaluation.FileChange;
import com.hackassist.ai.models.evaluation.FileSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileSummarizationService {

    private static final int MAX_LINES = 300;

    private static final String PROMPT_TEMPLATE =
        "Summarize what this code file does in 1-2 lines. Focus on functionality.\n\n" +
        "File: %s\n\n" +
        "Content:\n%s";

    private static final String BATCH_PROMPT_TEMPLATE =
        "Summarize what each code file does in 1-2 lines. Focus on functionality. " +
        "Return ONLY a JSON array (no markdown, no extra text, no code blocks). " +
        "Format: [{\"fileName\":\"...\",\"summary\":\"...\"}]\n\n" +
        "%s";

    private final GeminiService geminiService;

    public FileSummarizationService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public FileSummary summarize(FileChange fileChange) {
        if (fileChange == null || fileChange.getContent() == null) {
            throw new RuntimeException("File content is required for summarization");
        }
        String prompt = String.format(PROMPT_TEMPLATE, fileChange.getFilename(), trimToLines(fileChange.getContent()));
        String summary = geminiService.generate(prompt);
        log.debug("File summary generated for {}", fileChange.getFilename());
        return new FileSummary(fileChange.getFilename(), summary);
    }

    public List<FileSummary> summarizeFiles(List<FileChange> fileChanges) {
        if (fileChanges == null || fileChanges.isEmpty()) {
            return List.of();
        }

        String payload = fileChanges.stream()
            .map((file) -> "File: " + file.getFilename() + "\nContent:\n" + trimToLines(file.getContent()))
            .collect(Collectors.joining("\n\n"));

        String prompt = String.format(BATCH_PROMPT_TEMPLATE, payload);
        List<FileSummary> summaries = geminiService.generateJson(prompt, new TypeReference<List<FileSummary>>() {});

        if (summaries == null) {
            return List.of();
        }

        List<FileSummary> sanitized = new ArrayList<>();
        for (FileSummary summary : summaries) {
            if (summary == null || summary.getFileName() == null) {
                continue;
            }
            sanitized.add(summary);
        }
        return sanitized;
    }

    private String trimToLines(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String[] lines = content.split("\\R");
        int limit = Math.min(lines.length, MAX_LINES);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            builder.append(lines[i]).append("\n");
        }
        return builder.toString().trim();
    }
}
