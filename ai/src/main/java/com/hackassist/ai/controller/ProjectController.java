package com.hackassist.ai.controller;

import com.hackassist.ai.Service.IProjectService;
import com.hackassist.ai.Service.ProjectPlanningService;
import com.hackassist.ai.dto.plan.ProjectPlanDTO;
import com.hackassist.ai.dto.ProjectRepoLinkRequest;
import com.hackassist.ai.dto.ProjectRequest;
import com.hackassist.ai.dto.ProjectResponse;
import com.hackassist.ai.dto.ProjectUpdateRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/projects")
@CrossOrigin(origins = "*")
@Slf4j
public class ProjectController {
    private final IProjectService projectService;
    private final ProjectPlanningService planningService;

    public ProjectController(IProjectService projectService, ProjectPlanningService planningService) {
        this.projectService = projectService;
        this.planningService = planningService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest request) {
        String userId = getCurrentUserId();
        ProjectResponse response = projectService.createProject(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProjectResponse>> getMyProjects() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(projectService.getProjectsByUser(userId));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
        @PathVariable String projectId,
        @RequestBody ProjectUpdateRequest request
    ) {
        String userId = getCurrentUserId();
        try {
            ProjectResponse response = projectService.updateProject(projectId, request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId) {
        String userId = getCurrentUserId();
        try {
            projectService.deleteProject(projectId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PutMapping("/{projectId}/repo")
    public ResponseEntity<ProjectResponse> linkRepository(
        @PathVariable String projectId,
        @RequestBody ProjectRepoLinkRequest request
    ) {
        String userId = getCurrentUserId();
        try {
            ProjectResponse response = projectService.linkRepository(projectId, request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PostMapping("/{projectId}/finalize-tasks")
    public ResponseEntity<Void> finalizeTasks(
        @PathVariable String projectId,
        @RequestBody ProjectPlanDTO plan
    ) {
        String userId = getCurrentUserId();
        try {
            log.info("=== ProjectController.finalizeTasks ===");
            log.info("projectId: {}", projectId);
            log.info("userId: {}", userId);
            log.info("Received plan - problemStatement: '{}'", plan.getProblemStatement());
            log.info("Received plan - features count: {}", plan.getFeatures() != null ? plan.getFeatures().size() : 0);
            log.info("Received plan - modules count: {}", plan.getModules() != null ? plan.getModules().size() : 0);
            log.info("Received plan - tasks count: {}", plan.getTasks() != null ? plan.getTasks().size() : 0);
            planningService.saveProjectPlan(projectId, plan, userId);
            log.info("Project plan saved successfully");
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            log.error("Error saving project plan: {}", ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping("/{projectId}/plan")
    public ResponseEntity<ProjectPlanDTO> getProjectPlan(@PathVariable String projectId) {
        String userId = getCurrentUserId();
        try {
            ProjectPlanDTO plan = planningService.getProjectPlan(projectId, userId);
            return ResponseEntity.ok(plan);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
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
