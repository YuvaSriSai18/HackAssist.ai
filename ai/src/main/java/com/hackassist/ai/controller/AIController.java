package com.hackassist.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.hackassist.ai.Service.IAIService;
import com.hackassist.ai.dto.ProblemStatementRequestDTO;
import com.hackassist.ai.dto.AITasksResponseDTO;
import com.hackassist.ai.dto.FeatureDTO;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
@Slf4j
public class AIController {
    
    @Autowired
    private IAIService aiService;
    
    @PostMapping("/generate-tasks")
    public ResponseEntity<?> generateTasks(@RequestBody ProblemStatementRequestDTO request) {
        try {
            log.info("Generating tasks from problem statement");
            AITasksResponseDTO response = aiService.generateTasksFromProblemStatement(
                request.getProblemStatement(),
                request.getHackathonTheme()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating tasks", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate tasks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/generate-features")
    public ResponseEntity<?> generateFeatures(@RequestBody Map<String, String> request) {
        try {
            String problemStatement = request.get("problemStatement");
            List<FeatureDTO> features = aiService.generateFeatures(problemStatement);
            return ResponseEntity.ok(features);
        } catch (Exception e) {
            log.error("Error generating features", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate features: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/summary/{projectId}")
    public ResponseEntity<?> getProjectSummary(@PathVariable Long projectId) {
        try {
            String summary = aiService.generateProjectSummary(projectId);
            Map<String, String> response = new HashMap<>();
            response.put("summary", summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating summary", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate summary: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "AI Service is running");
        response.put("geminiIntegration", "Ready to process problem statements");
        return ResponseEntity.ok(response);
    }
}
