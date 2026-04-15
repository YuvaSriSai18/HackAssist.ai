package com.hackassist.ai.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_alerts")
@Check(constraints = "risk_type in ('NO_COMMITS_IN_HOURS','TOO_MANY_PENDING_TASKS','MISSED_DEADLINE','TEAM_MEMBER_INACTIVE','LOW_COMMIT_FREQUENCY','INCOMPLETE_TASK_DESCRIPTION') AND severity in ('LOW','MEDIUM','HIGH','CRITICAL') AND status in ('ACTIVE','ACKNOWLEDGED','RESOLVED')")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAlert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RiskType riskType;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;
    
    @Column(nullable = false)
    private LocalDateTime detectedAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String suggestedAction;
    
    @PrePersist
    protected void onCreate() {
        detectedAt = LocalDateTime.now();
        status = AlertStatus.ACTIVE;
    }
}
