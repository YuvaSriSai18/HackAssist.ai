package com.hackassist.ai.repository;

import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.ProjectModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectModuleRepository extends JpaRepository<ProjectModule, Long> {
    List<ProjectModule> findByProject(Project project);
    void deleteByProject(Project project);
}
