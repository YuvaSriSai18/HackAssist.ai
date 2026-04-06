package com.hackassist.ai.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskDTO {
    private String title;
    private String impact;
    private String mitigation;
}
