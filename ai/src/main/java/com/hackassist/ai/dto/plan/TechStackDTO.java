package com.hackassist.ai.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechStackDTO {
    private String backend;
    private String frontend;
    private String database;
    private String architecture;
}
