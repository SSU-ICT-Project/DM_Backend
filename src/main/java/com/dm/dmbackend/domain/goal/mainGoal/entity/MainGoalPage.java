package com.dm.dmbackend.domain.goal.mainGoal.entity;

import lombok.Data;
import lombok.Getter;

@Data
public class MainGoalPage {
    // 기본 page, size
    private int page = 0;
    private int size = 20;
    @Getter
    private static final int maxPageSize = 20;
}
