package com.hackassist.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberActivityDTO {
    private String memberUid;
    private String memberName;
    private Integer assignedTasks;
    private Integer completedTasks;
    private Integer totalCommits;
    private LocalDateTime lastActivityTime;
    private String activityStatus;
}
