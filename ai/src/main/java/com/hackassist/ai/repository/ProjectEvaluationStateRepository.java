package com.hackassist.ai.repository;

import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.ProjectEvaluationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProjectEvaluationStateRepository extends JpaRepository<ProjectEvaluationState, Long> {
    Optional<ProjectEvaluationState> findByProject(Project project);

    Optional<ProjectEvaluationState> findByProjectId(Long projectId);
}
