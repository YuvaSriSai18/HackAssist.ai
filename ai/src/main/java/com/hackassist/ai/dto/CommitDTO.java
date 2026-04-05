package com.hackassist.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitDTO {
    private Long id;
    private String commitHash;
    private String author;
    private String message;
    private LocalDateTime commitDate;
    private Long repositoryId;
    private Long mappedTaskId;
    private LocalDateTime createdAt;
    private Integer additions;
    private Integer deletions;
    private Integer fileChanges;
}
