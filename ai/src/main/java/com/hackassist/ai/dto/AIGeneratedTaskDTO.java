package com.hackassist.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIGeneratedTaskDTO {
    private String title;
    private String description;
    private String priority;
    private Integer estimatedHours;
    private String category;
    private String suggestedAssignee;
}
