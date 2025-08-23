package com.dm.dmbackend.domain.goal.subGoal.dto.res;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubGoalResponse {
    private Long id;
    private Long mainGoalId;
    private String content;
    private String dDay;
    private Boolean checked;
    private LocalDateTime createdAt;
}
