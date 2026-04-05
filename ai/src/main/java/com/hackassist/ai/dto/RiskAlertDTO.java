package com.hackassist.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAlertDTO {
    private Long id;
    private Long projectId;
    private String riskType;
    private String description;
    private String severity;
    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;
    private String status;
    private String suggestedAction;
}
