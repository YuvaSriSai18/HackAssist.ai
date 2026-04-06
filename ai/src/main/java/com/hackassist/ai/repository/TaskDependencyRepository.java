package com.hackassist.ai.repository;

import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    List<TaskDependency> findByProject(Project project);
    void deleteByProject(Project project);
}
