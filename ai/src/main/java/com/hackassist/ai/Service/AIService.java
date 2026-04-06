package com.hackassist.ai.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackassist.ai.dto.plan.ProjectPlanDTO;
import com.hackassist.ai.dto.plan.TechStackDTO;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.User;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AIService implements IAIService {

    private static final TechStackDTO STACK_CONTEXT = new TechStackDTO(
        "Spring Boot (Java)",
        "React + Tailwind + shadcn",
        "MySQL/PostgreSQL",
        "REST APIs"
    );

    private final GeminiService geminiService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIService(GeminiService geminiService, ProjectRepository projectRepository, UserRepository userRepository) {
        this.geminiService = geminiService;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ProjectPlanDTO generatePlan(String projectId, String problemStatement, String userId) {
        log.info("=== AIService.generatePlan ===");
        log.info("projectId: {}, userId: {}", projectId, userId);
        
        if (projectId == null || projectId.isBlank()) {
            log.error("projectId is null or blank");
            throw new RuntimeException("Project id is required");
        }
        if (problemStatement == null || problemStatement.isBlank()) {
            log.error("problemStatement is null or blank");
            throw new RuntimeException("Problem statement is required");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found with userId: {}", userId);
                return new RuntimeException("User not found");
            });
        log.info("User found: {}", user.getEmail());
        
        Project project = projectRepository.findByProjectIdAndCreatedBy(projectId, user);
        if (project == null) {
            log.error("Project not found or not owned by user. projectId: {}, userId: {}", projectId, userId);
            throw new RuntimeException("Project not found or not owned by user");
        }
        log.info("Project found: {}", project.getName());

        String prompt = buildPrompt(projectId, project.getName(), problemStatement);
        log.info("Calling Gemini API with prompt...");
        String response = geminiService.generateContent(prompt);
        log.info("Gemini response received, parsing...");
        ProjectPlanDTO plan = parseResponse(response);
        log.info("Plan parsed successfully");

        plan.setProjectId(projectId);
        plan.setProblemStatement(problemStatement);
        if (plan.getTechStack() == null) {
            plan.setTechStack(STACK_CONTEXT);
        }
        log.info("Plan generation completed successfully");
        return plan;
    }

    private String buildPrompt(String projectId, String projectName, String problemStatement) {
        return "You are a senior software planner generating an implementation plan.\n" +
            "Constraints:\n" +
            "- Backend: Spring Boot (Java)\n" +
            "- Frontend: React + Tailwind + shadcn\n" +
            "- Database: MySQL/PostgreSQL\n" +
            "- Architecture: REST APIs\n" +
            "\n" +
            "Project Id: " + projectId + "\n" +
            "Project Name: " + (projectName == null ? "" : projectName) + "\n" +
            "Problem Statement: " + problemStatement + "\n" +
            "\n" +
            "Return STRICT JSON ONLY with this schema:\n" +
            "{\n" +
            "  \"projectId\": \"" + projectId + "\",\n" +
            "  \"problemStatement\": \"...\",\n" +
            "  \"techStack\": {\n" +
            "    \"backend\": \"Spring Boot (Java)\",\n" +
            "    \"frontend\": \"React + Tailwind + shadcn\",\n" +
            "    \"database\": \"MySQL/PostgreSQL\",\n" +
            "    \"architecture\": \"REST APIs\"\n" +
            "  },\n" +
            "  \"features\": [\n" +
            "    {\"key\": \"F-001\", \"name\": \"...\", \"description\": \"...\", \"priority\": \"HIGH\"}\n" +
            "  ],\n" +
            "  \"modules\": [\n" +
            "    {\"key\": \"M-001\", \"name\": \"...\", \"description\": \"...\"}\n" +
            "  ],\n" +
            "  \"tasks\": [\n" +
            "    {\n" +
            "      \"externalId\": \"TSK-001\",\n" +
            "      \"title\": \"...\",\n" +
            "      \"description\": \"...\",\n" +
            "      \"priority\": \"HIGH|MEDIUM|LOW\",\n" +
            "      \"status\": \"TODO|IN_PROGRESS|DONE\",\n" +
            "      \"estimatedHours\": 4,\n" +
            "      \"moduleKey\": \"M-001\",\n" +
            "      \"dependsOn\": [\"TSK-000\"]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"risks\": [\n" +
            "    {\"title\": \"...\", \"impact\": \"...\", \"mitigation\": \"...\"}\n" +
            "  ]\n" +
            "}\n" +
            "Only output JSON. No markdown, no explanations.";
    }

    private ProjectPlanDTO parseResponse(String response) {
        try {
            String json = extractJson(response);
            return objectMapper.readValue(json, ProjectPlanDTO.class);
        } catch (Exception ex) {
            log.error("Invalid AI JSON response", ex);
            throw new RuntimeException("Invalid AI JSON response");
        }
    }

    private String extractJson(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new RuntimeException("AI response is empty");
        }

        String cleaned = rawResponse
            .replace("```json", "")
            .replace("```", "")
            .trim();

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end < 0 || end <= start) {
            throw new RuntimeException("AI response does not contain valid JSON");
        }

        return cleaned.substring(start, end + 1).trim();
    }
}
