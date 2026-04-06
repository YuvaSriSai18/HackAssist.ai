package com.hackassist.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.User;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    List<Project> findByCreatedBy(User user);
    
    List<Project> findByCreatedByOrderByCreatedAtDesc(User user);

    Project findByProjectId(String projectId);

    Project findByProjectIdAndCreatedBy(String projectId, User user);

    default List<Project> findByOwner(User user) {
        return findByCreatedBy(user);
    }
}
