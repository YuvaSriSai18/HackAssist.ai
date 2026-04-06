package com.hackassist.ai.controller;

import com.hackassist.ai.Service.IAIService;
import com.hackassist.ai.dto.plan.GenerateTasksRequest;
import com.hackassist.ai.dto.plan.ProjectPlanDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
@Slf4j
public class AIController {

    private final IAIService aiService;

    public AIController(IAIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate-tasks")
    public ResponseEntity<?> generateTasks(@RequestBody GenerateTasksRequest request) {
        String userId = getCurrentUserId();
        log.info("=== AIController.generateTasks ===");
        log.info("Request projectId: {}", request.getProjectId());
        log.info("Request problemStatement: {}", request.getProblemStatement());
        log.info("UserId: {}", userId);
        try {
            ProjectPlanDTO response = aiService.generatePlan(
                request.getProjectId(),
                request.getProblemStatement(),
                userId
            );
            log.info("Successfully generated plan for project: {}", request.getProjectId());
            log.info("Response problemStatement: '{}'", response.getProblemStatement());
            log.info("Response projectId: '{}'", response.getProjectId());
            log.info("Response features count: {}", response.getFeatures() != null ? response.getFeatures().size() : 0);
            log.info("Response modules count: {}", response.getModules() != null ? response.getModules().size() : 0);
            log.info("Response tasks count: {}", response.getTasks() != null ? response.getTasks().size() : 0);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            log.error("Error generating plan: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("AI generation failed");
        }
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getPrincipal().toString();
    }
}
