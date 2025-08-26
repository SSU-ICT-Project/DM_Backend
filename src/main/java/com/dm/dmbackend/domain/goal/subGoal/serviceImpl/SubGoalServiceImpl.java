package com.dm.dmbackend.domain.goal.subGoal.serviceImpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.goal.mainGoal.entity.MainGoal;
import com.dm.dmbackend.domain.goal.mainGoal.repository.MainGoalRepository;
import com.dm.dmbackend.domain.goal.subGoal.dto.req.SubGoalRequest;
import com.dm.dmbackend.domain.goal.subGoal.entity.SubGoal;
import com.dm.dmbackend.domain.goal.subGoal.repository.SubGoalRepository;
import com.dm.dmbackend.domain.goal.subGoal.service.SubGoalService;
import com.dm.dmbackend.global.exception.ReturnCode;
import com.dm.dmbackend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubGoalServiceImpl implements SubGoalService {
    private final SubGoalRepository subGoalRepository;
    private final MainGoalRepository mainGoalRepository;

    // 하위목표 생성
    @Override
    @Transactional
    public void addSubGoal(SubGoalRequest subGoalRequest, LoginUserDto loginUser){
        MainGoal mainGoal = mainGoalRepository.findById(subGoalRequest.getMainGoalId())
                .orElseThrow(() -> new ServiceException(ReturnCode.MAIN_GOAL_NOT_FOUND));
        SubGoal subGoal = SubGoal.builder()
                .member(loginUser.ConvertToMember())
                .mainGoal(mainGoal)
                .content(subGoalRequest.getContent())
                .deadline(subGoalRequest.getDeadline())
                .checked(subGoalRequest.getChecked())
                .build();
        subGoalRepository.save(subGoal);
    }

    // 하위목표 수정
    @Override
    @Transactional
    public void updateSubGoal(Long subGoalId, SubGoalRequest subGoalRequest, LoginUserDto loginUser){
        SubGoal subGoal = subGoalRepository.findById(subGoalId)
                .orElseThrow(() -> new ServiceException(ReturnCode.SUB_GOAL_NOT_FOUND));
        // 작성자 검증 아닌 경우 예외 처리
        validateSubGoalOwner(subGoal, loginUser);
        if (subGoalRequest.getContent() != null) {
            subGoal.setContent(subGoalRequest.getContent());
        }
        if (subGoalRequest.getDeadline() != null) {
            subGoal.setDeadline(subGoalRequest.getDeadline());
        }
        if (subGoalRequest.getChecked() != null) { // Boolean 래퍼 타입이어야 null 체크 가능
            subGoal.setChecked(subGoalRequest.getChecked());
        }
        subGoalRepository.save(subGoal);
    }

    // 하위목표 삭제
    @Override
    @Transactional
    public void deleteSubGoal(Long subGoalId, LoginUserDto loginUser) {
        SubGoal subGoal = subGoalRepository.findById(subGoalId)
                .orElseThrow(() -> new ServiceException(ReturnCode.SUB_GOAL_NOT_FOUND));
        // 작성자 검증 아닌 경우 예외 처리
        validateSubGoalOwner(subGoal, loginUser);
        subGoalRepository.delete(subGoal);
    }

    // 상위목표 내의 하위목표 전체 삭제
    @Override
    @Transactional
    public void deleteAllSubGoal(Long mainGoalId){
        List<SubGoal> subGoals = subGoalRepository.findByMainGoalId(mainGoalId);
        subGoalRepository.deleteAll(subGoals);
    }

    // ----------------- 헬퍼 메서드 -----------------

    // 목표 작성자 검증
    public static void validateSubGoalOwner(SubGoal subGoal, LoginUserDto loginUser) {
        if (!subGoal.getMember().getId().equals(loginUser.getId())) {
            throw new ServiceException(ReturnCode.NOT_AUTHORIZED);
        }
    }
}
