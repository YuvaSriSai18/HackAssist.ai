package com.hackassist.ai.controller;

import com.hackassist.ai.Service.ProjectProgressService;
import com.hackassist.ai.dto.ProjectProgressResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
public class ProjectProgressController {

    private final ProjectProgressService projectProgressService;

    public ProjectProgressController(ProjectProgressService projectProgressService) {
        this.projectProgressService = projectProgressService;
    }

    @GetMapping("/{projectId}/progress")
    public ResponseEntity<ProjectProgressResponse> getProgress(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectProgressService.getProjectProgress(projectId));
    }
}
