package com.hackassist.ai.Service;

import com.hackassist.ai.dto.ProjectRepoLinkRequest;
import com.hackassist.ai.dto.ProjectRequest;
import com.hackassist.ai.dto.ProjectResponse;
import com.hackassist.ai.dto.ProjectUpdateRequest;
import java.util.List;

public interface IProjectService {
    
    ProjectResponse createProject(ProjectRequest request, String userId);

    List<ProjectResponse> getProjectsByUser(String userId);

    ProjectResponse updateProject(String projectId, ProjectUpdateRequest request, String userId);

    void deleteProject(String projectId, String userId);

    ProjectResponse linkRepository(String projectId, ProjectRepoLinkRequest request, String userId);
}
