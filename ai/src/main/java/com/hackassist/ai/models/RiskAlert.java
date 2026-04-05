package com.hackassist.ai.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_alerts")
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
