package com.dm.dmbackend.domain.llm.screenTime.serviceImpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeReviewRequest;
import com.dm.dmbackend.domain.llm.screenTime.dto.res.ScreenTimeReviewResponse;
import com.dm.dmbackend.domain.llm.screenTime.entity.ScreenTime;
import com.dm.dmbackend.domain.llm.screenTime.repository.ScreenTimeRepository;
import com.dm.dmbackend.domain.llm.screenTime.service.ScreenTimeService;
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
    private final ScreenTimeRepository screenTimeRepository; // db와 통신
    private final ChatModel chatModel; // LLM 대화
    private final EmbeddingStoreContentRetriever retriever; //리트리버 RAG의 핵심

    // 스크린타임리뷰 메시지 생성
    @Override
    @Transactional
    public ScreenTimeReviewResponse getScreenTimeReview(ScreenTimeReviewRequest screenTimeReviewRequest, LoginUserDto loginUser) { //핵심 기능, 보낼 정보 준비
        String screenTimeData = screenTimeReviewRequest.getScreenTimeData();
        String motivationType = mapMotivationType(loginUser.getMotivationType());
//        String goalData = loginUser.getGoalData();   // 목표 데이터 필요
        LocalDate birthday = loginUser.getBirthday();
        int age = Period.between(birthday, LocalDate.now()).getYears();
        String userData = String.format("나이: %d, 성별: %s, 직업: %s",
                age, loginUser.getGender(), loginUser.getJob());

        PromptTemplate template = PromptTemplate.from( // 프롬프트 템플릿
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
                - {{motivation_type}}에 해당하는 내용을 {{context}}에서 찾으세요.
                - 디지털 과사용 (숏폼 중독, 습관적 sns 사용)으로 인해 일상에 지장이 생기는 사람들을 위해
                  사용자의 성향과 정보(나이, 성별, 직업)에 맞춘 동기부여 메시지를 제공하세요.
                - 긍정적이고 행동을 유도하는 톤을 유지하세요.
                """
        );

        // Retriever로 논문 검색
        List<Content> contents = retriever.retrieve(Query.from(screenTimeData));

        // 검색된 자료들 하나의 텍스트로 합치기
        String context = contents.stream()
                .map(c -> (c instanceof TextSegment ts) ? ts.text() : c.toString())
                .collect(Collectors.joining("\n"));

        // 동기부여 타입에 대한 상세 설명 준비
        String motivationPrompt = mapMotivationPrompt(loginUser.getMotivationType());

        // 프롬프트 완성하고 질문하기
        String prompt = template.apply(Map.of(
                "motivation_type", motivationPrompt,
                "screen_time_data", screenTimeData,
                "goal_data", "",      // 목표 데이터 필요
                "user_data", userData,
                "context", context
        )).text();

        // ChatModel 실행
        String reviewMessage = chatModel.chat(prompt);

        // LLM답변을 DB에 저장
        ScreenTime screenTime = ScreenTime.builder()
                .member(loginUser.ConvertToMember())
                .screenTimeData(screenTimeData)
                .review(reviewMessage)
                .build();
        screenTimeRepository.save(screenTime);
        // 최종 결과를 앱에 가공해서 반환
        return screenTimeConvertToScreenTimeReviewResponse(screenTime);
    }

    // ----------------- 헬퍼 메서드 -----------------

    // 동기부여 한글 매핑
    private String mapMotivationType(Member.MotivationType type) {
        return switch (type) {
            case ACHIEVER -> "성취형";
            case MINDFUL -> "감정형";
            case CHALLENGER -> "도전형";
        };
    }

    // 동기부여 프롬프트 매핑
    private String mapMotivationPrompt(Member.MotivationType type) {
        return switch (type) {
            case ACHIEVER -> """
            성취형:
            - 사용자는 불안, 스트레스에서 벗어나기 위해 목표를 세우는 유형(감정형)입니다.
            - 사용자의 목표는 감정적 회복의 도구가 되지만, 스트레스가 커지면 쉽게 포기할 위험도 있습니다.
            - 사용자가 꾸준히 노력할 수 있도록, 사용자의 목표를 기반으로 동기부여해야합니다.
            """;
            case MINDFUL -> """
            감성형:
            - 사용자는 새로운 자극과 도전에서 힘을 얻는 유형(도전형)입니다.
            - 사용자는 즉각적인 성취와 변화에 강하게 동기부여 되지만, 장기적인 계획에는 쉽게 흔들릴 수 있습니다.
            - 사용자가 안정적으로 목표를 이어갈 수 있도록 사용자의 목표를 기반으로 동기부여해야합니다.
            """;
            case CHALLENGER -> """
            도전형:
            - 사용자는 새로운 자극과 도전에서 힘을 얻는 유형(도전형)입니다.
            - 즉각적인 성취와 변화에 강하게 동기부여 되지만, 장기적인 계획에는 쉽게 흔들릴 수 있습니다.
            - 사용자가 장기 목표도 놓치지 않도록 짧고  사용자의 목표를 기반으로 동기부여해야합니다.
            """;
        };
    }

    // ScreenTime을 ScreenTimeReviewResponse로 변환, DB객체를 앱 응답 객체로 변환
    private ScreenTimeReviewResponse screenTimeConvertToScreenTimeReviewResponse(ScreenTime screenTime) {
        return ScreenTimeReviewResponse.builder()
                .id(screenTime.getId())
                .review(screenTime.getReview())
                .build();
    }
}

