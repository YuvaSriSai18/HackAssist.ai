package com.hackassist.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemStatementRequestDTO {
    private Long projectId;
    private String problemStatement;
    private String hackathonTheme;
}
