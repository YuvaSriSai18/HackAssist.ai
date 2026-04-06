package com.hackassist.ai.repository;

import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {
    List<ProjectTask> findByProject(Project project);
    void deleteByProject(Project project);
    ProjectTask findByProjectAndExternalId(Project project, String externalId);
}
