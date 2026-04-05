package com.hackassist.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.hackassist.ai.Service.IIntelligenceService;
import com.hackassist.ai.dto.RiskAlertDTO;
import com.hackassist.ai.dto.ProjectSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/intelligence")
@CrossOrigin(origins = "*")
@Slf4j
public class IntelligenceController {
    
    @Autowired
    private IIntelligenceService intelligenceService;
    
    @PostMapping("/detect-risks/{projectId}")
    public ResponseEntity<?> detectRisks(@PathVariable Long projectId) {
        try {
            List<RiskAlertDTO> risks = intelligenceService.detectRisks(projectId);
            return ResponseEntity.ok(risks);
        } catch (RuntimeException e) {
            log.error("Error detecting risks", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/summary/{projectId}")
    public ResponseEntity<?> getProjectSummary(@PathVariable Long projectId) {
        try {
            ProjectSummaryDTO summary = intelligenceService.generateProjectSummaryForPresentation(projectId);
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            log.error("Error generating project summary", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PostMapping("/suggest-actions/{projectId}")
    public ResponseEntity<?> suggestNextActions(@PathVariable Long projectId) {
        try {
            String suggestions = intelligenceService.suggestNextActions(projectId);
            Map<String, String> response = new HashMap<>();
            response.put("suggestions", suggestions);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error suggesting actions", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
