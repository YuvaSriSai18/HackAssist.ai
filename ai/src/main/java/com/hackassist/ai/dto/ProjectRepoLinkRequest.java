package com.hackassist.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRepoLinkRequest {
    private String repoUrl;
    private String repoFullName;
}
