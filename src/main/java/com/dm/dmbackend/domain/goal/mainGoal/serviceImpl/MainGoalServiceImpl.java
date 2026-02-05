package com.dm.dmbackend.domain.goal.mainGoal.serviceImpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.goal.mainGoal.dto.req.MainGoalRequest;
import com.dm.dmbackend.domain.goal.mainGoal.dto.res.MainGoalResponse;
import com.dm.dmbackend.domain.goal.mainGoal.dto.res.MainGoalWithSubGoalsResponse;
import com.dm.dmbackend.domain.goal.mainGoal.entity.MainGoal;
import com.dm.dmbackend.domain.goal.mainGoal.entity.MainGoalPage;
import com.dm.dmbackend.domain.goal.mainGoal.repository.MainGoalRepository;
import com.dm.dmbackend.domain.goal.mainGoal.service.MainGoalService;
import com.dm.dmbackend.domain.goal.subGoal.dto.res.SubGoalResponse;
import com.dm.dmbackend.domain.goal.subGoal.entity.SubGoal;
import com.dm.dmbackend.domain.goal.subGoal.repository.SubGoalRepository;
import com.dm.dmbackend.domain.goal.subGoal.service.SubGoalService;
import com.dm.dmbackend.global.common.response.PageResponse;
import com.dm.dmbackend.global.exception.ReturnCode;
import com.dm.dmbackend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainGoalServiceImpl implements MainGoalService {
    private final MainGoalRepository mainGoalRepository;
    private final SubGoalRepository subGoalRepository;
    private final SubGoalService subGoalService;

    // 목표 생성
    @Override
    @Transactional
    public void addMainGoal(MainGoalRequest mainGoalRequest, LoginUserDto loginUser){
        MainGoal mainGoal = MainGoal.builder()
                .member(loginUser.ConvertToMember())
                .content(mainGoalRequest.getContent())
                .deadline(mainGoalRequest.getDeadline())
                .checked(mainGoalRequest.getChecked())
                .build();
        mainGoalRepository.save(mainGoal);
    }

    // 목표 전체 리스트 조회
    @Override
    @Transactional(readOnly = true)
    public PageResponse<MainGoalWithSubGoalsResponse> getAllGoal(Pageable pageable, LoginUserDto loginUser) {
        checkPageSize(pageable.getPageSize());

        // 1) 메인 목표 페이지 조회 (DB에서 NULLS LAST 처리)
        Page<MainGoal> mainPage = mainGoalRepository.findByMemberIdWithOrder(loginUser.getId(), pageable);

        // 2) 해당 페이지에 포함된 메인 목표 ID 추출
        List<Long> mainIds = mainPage.getContent().stream()
                .map(MainGoal::getId)
                .toList();
        // 3) 하위 목표 벌크 조회 (DB에서 mainGoal.id ASC, deadline ASC NULLS LAST 처리)
        List<SubGoal> subGoals = mainIds.isEmpty()
                ? List.of()
                : subGoalRepository.findByMainGoalIdInWithOrder(mainIds);

        // 4) 하위 목표들을 mainGoalId 기준으로 그룹핑
        Map<Long, List<SubGoal>> subByMainId = new LinkedHashMap<>();
        for (SubGoal sg : subGoals) {
            Long mid = sg.getMainGoal().getId();
            subByMainId.computeIfAbsent(mid, k -> new ArrayList<>()).add(sg);
        }
        // 5) DTO 변환
        List<MainGoalWithSubGoalsResponse> content = new ArrayList<>(mainPage.getNumberOfElements());
        for (MainGoal mg : mainPage.getContent()) {
            MainGoalResponse mainDto = convertToMainGoalResponse(mg);
            List<SubGoalResponse> subDtos = subByMainId.getOrDefault(mg.getId(), List.of())
                    .stream()
                    .map(this::convertToSubGoalResponse)
                    .toList();
            content.add(MainGoalWithSubGoalsResponse.builder()
                    .mainGoal(mainDto)
                    .subGoals(subDtos)
                    .build());
        }
        // 6) Page로 감싸서 반환
        Page<MainGoalWithSubGoalsResponse> page = new PageImpl<>(
                content,
                mainPage.getPageable(),
                mainPage.getTotalElements()
        );
        return PageResponse.of(page);
    }

    // 상위목표 조회(RAG용)
    @Override
    @Transactional(readOnly = true)
    public String buildCompactGoalSummary(LoginUserDto loginUser, int limit){
        Page<MainGoal> mainPage = mainGoalRepository.findForRag(
                loginUser.getId(), PageRequest.of(0, limit));

        List<MainGoal> mains = mainPage.getContent();
        if (mains.isEmpty()) return "";

        List<Long> mainIds = mains.stream().map(MainGoal::getId).toList();
        StringBuilder sb = new StringBuilder(512);
        int i = 1;
        for (MainGoal mg : mains) {
            String mainLine = "%d) [%s] %s".formatted(
                    i++,
                    calculateDDay(mg.getDeadline()),
                    safe(mg.getContent())
            );
            sb.append(mainLine).append('\n');
        }
        return sb.toString().trim();
    }

    // 목표 수정
    @Override
    @Transactional
    public void updateMainGoal(Long mainGoalId, MainGoalRequest mainGoalRequest, LoginUserDto loginUser){
        MainGoal mainGoal = mainGoalRepository.findById(mainGoalId).orElseThrow(() -> new ServiceException(ReturnCode.MAIN_GOAL_NOT_FOUND));
        // 작성자 검증 아닌 경우 예외 처리
        validateMainGoalOwner(mainGoal, loginUser);
        if (mainGoalRequest.getContent() != null) {
            mainGoal.setContent(mainGoalRequest.getContent());
        }
        if (mainGoalRequest.getDeadline() != null) {
            mainGoal.setDeadline(mainGoalRequest.getDeadline());
        }
        if (mainGoalRequest.getChecked() != null) {
            mainGoal.setChecked(mainGoalRequest.getChecked());
        }
        mainGoalRepository.save(mainGoal);
    }

    // 목표 삭제
    @Override
    @Transactional
    public void deleteMainGoal(Long mainGoalId, LoginUserDto loginUser) {
        MainGoal mainGoal = mainGoalRepository.findById(mainGoalId).orElseThrow(() -> new ServiceException(ReturnCode.MAIN_GOAL_NOT_FOUND));
        // 작성자 검증 아닌 경우 예외 처리
        validateMainGoalOwner(mainGoal, loginUser);
        deleteSubGoals(mainGoalId);
        mainGoalRepository.delete(mainGoal);
    }

    // 해당 목표의 모든 하위 목표 삭제
    private void deleteSubGoals(Long mainGoalId) {
        subGoalService.deleteAllSubGoal(mainGoalId);
    }

    // ----------------- 헬퍼 메서드 -----------------

    // 요청 페이지 수 제한
    private void checkPageSize(int pageSize) {
        int maxPageSize = MainGoalPage.getMaxPageSize();
        if (pageSize > maxPageSize) {
            throw new ServiceException(ReturnCode.PAGE_REQUEST_FAIL);
        }
    }

    // 목표 작성자 검증
    public static void validateMainGoalOwner(MainGoal mainGoal, LoginUserDto loginUser) {
        if (!mainGoal.getMember().getId().equals(loginUser.getId())) {
            throw new ServiceException(ReturnCode.NOT_AUTHORIZED);
        }
    }

    // MainGoal를 MainGoalResponse로 변환
    private MainGoalResponse convertToMainGoalResponse(MainGoal mainGoal) {
        return MainGoalResponse.builder()
                .id(mainGoal.getId())
                .content(mainGoal.getContent())
                .dDay(calculateDDay(mainGoal.getDeadline()))
                .checked(mainGoal.getChecked())
                .createdAt(mainGoal.getCreatedAt())
                .build();
    }

    // SubGoal를 SubGoalResponse로 변환
    private SubGoalResponse convertToSubGoalResponse(SubGoal subGoal) {
        return SubGoalResponse.builder()
                .id(subGoal.getId())
                .mainGoalId(subGoal.getMainGoal().getId())
                .content(subGoal.getContent())
                .dDay(calculateDDay(subGoal.getDeadline()))
                .checked(subGoal.getChecked())
                .createdAt(subGoal.getCreatedAt())
                .build();
    }

    // D-Day 계산 함수
    private String calculateDDay(LocalDate deadline) {
        if (deadline == null) {
            return null;
        }
        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        if (daysBetween > 0) {
            return "D-" + daysBetween;   // 마감일이 미래
        } else if (daysBetween == 0) {
            return "D-Day";              // 오늘이 마감일
        } else {
            return "D+" + Math.abs(daysBetween); // 마감일이 지남
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.replaceAll("\\s+", " ").trim();
    }
}
