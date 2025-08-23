package com.dm.dmbackend.domain.goal.mainGoal.service;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.goal.mainGoal.dto.req.MainGoalRequest;
import com.dm.dmbackend.domain.goal.mainGoal.dto.res.MainGoalWithSubGoalsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MainGoalService {
    // 목표 생성
    void addMainGoal(MainGoalRequest mainGoalRequest, LoginUserDto loginUser);

    // 목표 리스트 조회
    Page<MainGoalWithSubGoalsResponse> getAllGoal(Pageable pageable, LoginUserDto loginUser);

    // 목표 수정
    void updateMainGoal(Long mainGoalId, MainGoalRequest mainGoalRequest, LoginUserDto loginUser);

    // 목표 삭제
    void deleteMainGoal(Long mainGoalId, LoginUserDto loginUser);
}
