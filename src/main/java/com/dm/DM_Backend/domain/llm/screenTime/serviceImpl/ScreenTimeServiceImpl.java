package com.dm.DM_Backend.domain.llm.screenTime.serviceImpl;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.account.member.entity.Member;
import com.dm.DM_Backend.domain.llm.screenTime.dto.req.ScreenTimeReviewRequest;
import com.dm.DM_Backend.domain.llm.screenTime.dto.res.ScreenTimeReviewResponse;
import com.dm.DM_Backend.domain.llm.screenTime.entity.ScreenTime;
import com.dm.DM_Backend.domain.llm.screenTime.repository.ScreenTimeRepository;
import com.dm.DM_Backend.domain.llm.screenTime.service.ScreenTimeService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreenTimeServiceImpl implements ScreenTimeService {
    private final ScreenTimeRepository screenTimeRepository;
    private final ChatModel chatModel;
    private final EmbeddingStoreContentRetriever retriever;

    // 스크린타임리뷰 생성
    @Override
    @Transactional
    public ScreenTimeReviewResponse getScreenTimeReview(ScreenTimeReviewRequest request, LoginUserDto loginUser) {
        String screenTimeData = request.getScreenTimeData();
        String motivationType = mapMotivationType(loginUser.getMotivationType());
//        String goalData = loginUser.getGoalData();   // 목표 데이터 필요
        LocalDate birthday = loginUser.getBirthday();
        int age = Period.between(birthday, LocalDate.now()).getYears();
        String userData = String.format("나이: %d, 성별: %s, 직업: %s",
                age, loginUser.getGender(), loginUser.getJob());

        PromptTemplate template = PromptTemplate.from(
                """
                당신은 사용자의 스크린타임 코치입니다.
    
                [동기부여 형식]
                {{motivation_type}}
    
                [오늘의 스크린타임 데이터]
                {{screen_time_data}}
    
                [사용자의 목표]
                {{goal_data}}
    
                [사용자 정보]
                {{user_data}}
    
                [참고 문헌 기반 컨텍스트]
                {{context}}
    
                요구사항:
                - {{motivation_type}} 에 맞게 70자 내외 한국어 메시지를 작성하세요.
                - 디지털 과사용 (숏폼 중독, 습관적 sns 사용)으로 인해 일상에 지장이 생기는 사람들을 위해
                  사용자의 성향과 정보(나이, 성별, 직업)에 맞춘 동기부여 메시지를 제공하세요.
                - 긍정적이고 행동을 유도하는 톤을 유지하세요.
                """
        );

        // Retriever로 논문 검색
        List<Content> contents = retriever.retrieve(Query.from(screenTimeData));
        String context = contents.stream()
                .map(c -> (c instanceof TextSegment ts) ? ts.text() : c.toString())
                .collect(Collectors.joining("\n"));

        String motivationPrompt = mapMotivationPrompt(loginUser.getMotivationType());

        String prompt = template.apply(Map.of(
                "motivation_type", motivationPrompt,
                "screen_time_data", screenTimeData,
                "goal_data", "",      // 목표 데이터 필요
                    "user_data", userData,
                "context", context
        )).text();

        // ChatModel 실행
        String reviewMessage = chatModel.chat(prompt);

        // DB 저장
        ScreenTime screenTime = ScreenTime.builder()
                .member(loginUser.ConvertToMember())
                .screenTimeData(screenTimeData)
                .review(reviewMessage)
                .build();
        screenTimeRepository.save(screenTime);

        return screenTimeConvertToScreenTimeReviewResponse(screenTime);
    }

    private String mapMotivationType(Member.MotivationType type) {
        return switch (type) {
            case EMOTIONAL -> "감성 자극형";
            case VISION -> "미래/비전 제시형";
            case ACTION -> "구체적 행동 제시형";
            case COMPETITION -> "비교/경쟁 자극형";
        };
    }

    private String mapMotivationPrompt(Member.MotivationType type) {
        return switch (type) {
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

    // ScreenTime을 ScreenTimeReviewResponse로 변환
    private ScreenTimeReviewResponse screenTimeConvertToScreenTimeReviewResponse(ScreenTime screenTime) {
        return ScreenTimeReviewResponse.builder()
                .id(screenTime.getId())
                .review(screenTime.getReview())
                .build();
    }
}
