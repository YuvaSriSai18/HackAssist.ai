package com.hackassist.ai.Service;

import com.hackassist.ai.models.Project;
import java.util.List;

public interface IProjectService {
    
    Project createProject(Project project);
    
    Project getProjectById(Long id);
    
    Project updateProject(Project project);
    
    void deleteProject(Long id);
    
    List<Project> getAllProjects();
    
    List<Project> getProjectsByUser(String userId);
}
