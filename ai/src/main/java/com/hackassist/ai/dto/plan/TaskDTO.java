package com.hackassist.ai.dto.plan;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private String externalId;
    private String title;
    private String description;
    private String priority;
    private String status;
    private Integer estimatedHours;
    private String moduleKey;
    private List<String> dependsOn;
}
