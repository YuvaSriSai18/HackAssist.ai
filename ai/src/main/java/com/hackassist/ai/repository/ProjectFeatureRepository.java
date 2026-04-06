package com.hackassist.ai.repository;

import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.ProjectFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectFeatureRepository extends JpaRepository<ProjectFeature, Long> {
    List<ProjectFeature> findByProject(Project project);
    void deleteByProject(Project project);
}
