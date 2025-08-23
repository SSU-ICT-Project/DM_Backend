package com.dm.dmbackend.domain.goal.subGoal.service;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.goal.subGoal.dto.req.SubGoalRequest;

public interface SubGoalService {
    // 하위목표 생성
    void addSubGoal(SubGoalRequest subGoalRequest, LoginUserDto loginUser);

    // 하위목표 수정
    void updateSubGoal(Long subGoalId, SubGoalRequest subGoalRequest, LoginUserDto loginUser);

    // 하위목표 삭제
    void deleteSubGoal(Long subGoalId, LoginUserDto loginUser);

    // 상위목표 내의 하위목표 전체 삭제
    void deleteAllSubGoal(Long mainGoalId);
}
