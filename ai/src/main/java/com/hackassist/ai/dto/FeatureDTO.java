package com.hackassist.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureDTO {
    private String name;
    private String description;
    private String priority;
    private List<String> technologiesNeeded;
}
