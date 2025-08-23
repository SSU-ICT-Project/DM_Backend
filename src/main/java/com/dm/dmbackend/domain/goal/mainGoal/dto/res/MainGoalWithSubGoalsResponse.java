package com.dm.dmbackend.domain.goal.mainGoal.dto.res;

import com.dm.dmbackend.domain.goal.subGoal.dto.res.SubGoalResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MainGoalWithSubGoalsResponse {
    private MainGoalResponse mainGoal;
    private List<SubGoalResponse> subGoals;
}
