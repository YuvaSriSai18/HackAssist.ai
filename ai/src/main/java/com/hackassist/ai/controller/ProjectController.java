package com.hackassist.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.User;
import com.hackassist.ai.Service.IProjectService;
import com.hackassist.ai.Service.UserService;
import com.hackassist.ai.dto.ProjectDTO;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
@CrossOrigin(origins = "*")
@Slf4j
public class ProjectController {
    
    @Autowired
    private IProjectService projectService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO projectDTO) {
        try {
            User user = userService.getUserById(projectDTO.getCreatedByUid());
            Project project = new Project();
            project.setName(projectDTO.getName());
            project.setDescription(projectDTO.getDescription());
            project.setProblemStatement(projectDTO.getProblemStatement());
            project.setCreatedBy(user);
            project.setGithubRepoUrl(projectDTO.getGithubRepoUrl());
            
            Project savedProject = projectService.createProject(project);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id);
            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllProjects() {
        try {
            List<Project> projects = projectService.getAllProjects();
            return ResponseEntity.ok(projects);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve projects: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/user/{uid}")
    public ResponseEntity<?> getProjectsByUser(@PathVariable String uid) {
        try {
            List<Project> projects = projectService.getProjectsByUser(uid);
            return ResponseEntity.ok(projects);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody ProjectDTO projectDTO) {
        try {
            Project project = projectService.getProjectById(id);
            project.setName(projectDTO.getName());
            project.setDescription(projectDTO.getDescription());
            project.setProblemStatement(projectDTO.getProblemStatement());
            project.setGithubRepoUrl(projectDTO.getGithubRepoUrl());
            
            Project updatedProject = projectService.updateProject(project);
            return ResponseEntity.ok(updatedProject);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Project deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
