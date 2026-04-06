package com.hackassist.ai.repository;

import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.ProjectRisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRiskRepository extends JpaRepository<ProjectRisk, Long> {
    List<ProjectRisk> findByProject(Project project);
    void deleteByProject(Project project);
}
