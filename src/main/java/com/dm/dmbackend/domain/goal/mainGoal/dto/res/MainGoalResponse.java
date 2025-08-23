package com.dm.dmbackend.domain.goal.mainGoal.dto.res;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MainGoalResponse {
    private Long id;
    private String content;
    private String dDay;
    private Boolean checked;
    private LocalDateTime createdAt;
}
