package com.hackassist.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hackassist.ai.models.RiskAlert;
import com.hackassist.ai.models.Project;
import java.util.List;

@Repository
public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {
    
    List<RiskAlert> findByProject(Project project);

    void deleteByProject(Project project);
    
    List<RiskAlert> findByProjectAndStatus(Project project, String status);
}
