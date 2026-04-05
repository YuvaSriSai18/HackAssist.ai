package com.hackassist.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.hackassist.ai.Service.IProgressService;
import com.hackassist.ai.dto.ProgressDTO;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/progress")
@CrossOrigin(origins = "*")
@Slf4j
public class ProgressController {
    
    @Autowired
    private IProgressService progressService;
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getProjectProgress(@PathVariable Long projectId) {
        try {
            ProgressDTO progress = progressService.getProjectProgress(projectId);
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            log.error("Error fetching project progress", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/task-completion/{projectId}")
    public ResponseEntity<?> getTaskCompletion(@PathVariable Long projectId) {
        try {
            Double completion = progressService.calculateTaskCompletionPercentage(projectId);
            Map<String, Double> response = new HashMap<>();
            response.put("taskCompletionPercentage", completion);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error calculating task completion", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/commit-contribution/{projectId}")
    public ResponseEntity<?> getCommitContribution(@PathVariable Long projectId) {
        try {
            Double contribution = progressService.calculateCommitContribution(projectId);
            Map<String, Double> response = new HashMap<>();
            response.put("commitContributionPercentage", contribution);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error calculating commit contribution", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
