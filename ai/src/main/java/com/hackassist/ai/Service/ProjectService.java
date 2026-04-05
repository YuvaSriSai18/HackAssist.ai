package com.hackassist.ai.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.User;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProjectService implements IProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public Project createProject(Project project) {
        if (project.getCreatedBy() == null) {
            throw new RuntimeException("Project must have a creator");
        }
        if (!userRepository.existsById(project.getCreatedBy().getUid())) {
            throw new RuntimeException("User not found");
        }
        return projectRepository.save(project);
    }
    
    @Override
    public Project getProjectById(Long id) {
        Optional<Project> project = projectRepository.findById(id);
        if (!project.isPresent()) {
            throw new RuntimeException("Project not found with id: " + id);
        }
        return project.get();
    }
    
    @Override
    public Project updateProject(Project project) {
        if (!projectRepository.existsById(project.getId())) {
            throw new RuntimeException("Project not found with id: " + project.getId());
        }
        return projectRepository.save(project);
    }
    
    @Override
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
        log.info("Project deleted with id: {}", id);
    }
    
    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
    
    @Override
    public List<Project> getProjectsByUser(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return projectRepository.findByCreatedByOrderByCreatedAtDesc(user.get());
    }
}
