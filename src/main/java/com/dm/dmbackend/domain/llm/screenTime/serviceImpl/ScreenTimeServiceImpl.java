package com.dm.dmbackend.domain.llm.screenTime.serviceImpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.goal.mainGoal.service.MainGoalService;
import com.dm.dmbackend.domain.llm.screenTime.dto.internal.UserContext;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeCoachRequest;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeReviewRequest;
import com.dm.dmbackend.domain.llm.screenTime.dto.res.ScreenTimeMessageResponse;
import com.dm.dmbackend.domain.llm.screenTime.entity.ScreenTime;
import com.dm.dmbackend.domain.llm.screenTime.repository.ScreenTimeRepository;
import com.dm.dmbackend.domain.llm.screenTime.service.ScreenTimeCoach;
import com.dm.dmbackend.domain.llm.screenTime.service.ScreenTimeReview;
import com.dm.dmbackend.domain.llm.screenTime.service.ScreenTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreenTimeServiceImpl implements ScreenTimeService {
    private final ScreenTimeRepository screenTimeRepository;
    private final MainGoalService mainGoalService;
    private final ScreenTimeCoach coach;
    private final ScreenTimeReview review;

    // 스크린타임 리뷰 메시지 생성
    @Override
    @Transactional
    public ScreenTimeMessageResponse getScreenTimeReview(ScreenTimeReviewRequest screenTimeReviewRequest,
                                                         LoginUserDto loginUser) {
        String screenTimeData = screenTimeReviewRequest.getScreenTimeData();
        UserContext ctx = buildUserContext(loginUser);

        // 자동 RAG 호출 (Retriever가 pgvector에서 문맥을 가져와 {{information}}에 자동 주입)
        String reviewMessage = review.message(
                ctx.getMotivationPrompt(),
                nvl(screenTimeData),
                nvl(ctx.getGoalSummary()),
                ctx.getUserData()
        );
        // DB 저장
        ScreenTime screenTime = ScreenTime.builder()
                .member(loginUser.ConvertToMember())
                .screenTimeData(screenTimeData)
                .message(reviewMessage)
                .messageType(ScreenTime.MessageType.REVIEW)
                .build();
        screenTimeRepository.save(screenTime);
        return screenTimeConvertToScreenTimeMessageResponse(screenTime);
    }

    // 스크린타임 코칭 메시지 생성
    @Override
    @Transactional
    public ScreenTimeMessageResponse getScreenTimeCoach(ScreenTimeCoachRequest screenTimeCoachRequest,
                                                        LoginUserDto loginUser){
        String accessAppData = screenTimeCoachRequest.getAccessAppData();
        UserContext ctx = buildUserContext(loginUser);

        // 자동 RAG 호출 (Retriever가 pgvector에서 문맥을 가져와 {{information}}에 자동 주입)
        String coachMessage = coach.message(
                ctx.getMotivationPrompt(),
                nvl(accessAppData),
                nvl(ctx.getGoalSummary()),
                ctx.getUserData()
        );
        // DB 저장
        ScreenTime screenTime = ScreenTime.builder()
                .member(loginUser.ConvertToMember())
                .accessAppData(accessAppData)
                .message(coachMessage)
                .messageType(ScreenTime.MessageType.COACH)
                .build();
        screenTimeRepository.save(screenTime);
        return screenTimeConvertToScreenTimeMessageResponse(screenTime);
    }

    // ----------------- 헬퍼 메서드 -----------------

    private String nvl(Object v) { return v == null ? "" : String.valueOf(v); }

    // 동기부여 프롬프트 매핑
    private String mapMotivationPrompt(Member.MotivationType type) {
        Member.MotivationType t = (type == null) ? Member.MotivationType.ACTION : type;
        return switch (t) {
            case EMOTIONAL -> """
            감성 자극형:
            - 따뜻한 공감과 위로를 중심으로 메시지를 작성하세요.
            - 사용자의 감정을 이해하고 격려하는 톤을 사용하세요.
            """;
            case VISION -> """
            미래/비전 제시형:
            - 장기적인 목표와 긍정적인 미래를 강조하세요.
            - 사용자가 지금의 행동이 미래의 성취로 이어진다는 점을 부각하세요.
            """;
            case ACTION -> """
            구체적 행동 제시형:
            - 지금 바로 실천 가능한 구체적인 행동을 제안하세요.
            - 사용자가 즉시 따라할 수 있도록 명확한 지시를 포함하세요.
            """;
            case COMPETITION -> """
            비교/경쟁 자극형:
            - 다른 사람과의 비교나 경쟁심을 유발하는 메시지를 작성하세요.
            - 더 나은 성과를 향해 도전하도록 동기를 부여하세요.
            """;
        };
    }

    // 사용자 메타 + 목표 요약을 포함한 컨텍스트 데이터 생성
    private UserContext buildUserContext(LoginUserDto loginUser) {
        String motivationPrompt = mapMotivationPrompt(loginUser.getMotivationType());
        LocalDate birthday = loginUser.getBirthday();
        int age = birthday != null ? Period.between(birthday, LocalDate.now()).getYears() : 0;
        String userData = String.format("나이: %d, 성별: %s, 직업: %s",
                age, nvl(loginUser.getGender()), nvl(loginUser.getJob()));

        // RAG용 목표 요약 (상위 3개)
        String goalSummary = mainGoalService.buildCompactGoalSummary(loginUser, 3);
        return new UserContext(motivationPrompt, userData, goalSummary);
    }

    // ScreenTime을 ScreenTimeReviewResponse로 변환
    private ScreenTimeMessageResponse screenTimeConvertToScreenTimeMessageResponse(ScreenTime screenTime) {
        return ScreenTimeMessageResponse.builder()
                .id(screenTime.getId())
                .message(screenTime.getMessage())
                .build();
    }
}
