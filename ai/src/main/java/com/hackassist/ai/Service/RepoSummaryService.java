package com.hackassist.ai.Service;

import com.hackassist.ai.models.evaluation.FileSummary;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RepoSummaryService {

    private static final String PROMPT_TEMPLATE =
        "You are summarizing a repository based on file summaries. " +
        "Provide a concise project-level description in 3-5 sentences.\n\n" +
        "File Summaries:\n%s";

    private final GeminiService geminiService;

    public RepoSummaryService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public String buildRepoSummary(List<FileSummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            return "";
        }
        String summaryBlock = summaries.stream()
            .map((summary) -> "- " + summary.getFileName() + ": " + summary.getSummary())
            .collect(Collectors.joining("\n"));

        String prompt = String.format(PROMPT_TEMPLATE, summaryBlock);
        String repoSummary = geminiService.generate(prompt);
        log.debug("Repo summary generated ({} chars)", repoSummary == null ? 0 : repoSummary.length());
        return repoSummary;
    }
}
