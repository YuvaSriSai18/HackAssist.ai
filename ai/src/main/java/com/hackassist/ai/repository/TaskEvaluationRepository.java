package com.hackassist.ai.repository;

import com.hackassist.ai.models.TaskEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskEvaluationRepository extends JpaRepository<TaskEvaluation, Long> {
    Optional<TaskEvaluation> findByTaskId(Long taskId);
    List<TaskEvaluation> findByProjectIdOrderByLastEvaluatedAtDesc(Long projectId);
}
