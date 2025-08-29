package com.dm.dmbackend.domain.llm.screenTime.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ScreenTimeCure {
    @SystemMessage("""
            당신은 디지털 과사용 중독 치료 전문가입니다. 디지털 디톡스가 필요한 사용자의 데이터와 목표를 참고하여 디지털 중독 치료를 돕는 맞춤형 메시지를 생성하세요.
            최종 출력은 단 두 줄의 한국어 문장(40~50자)으로 작성하세요.
        
            [스크린타임 데이터]
            {{screen_time_data}}
        
            [사용자의 목표]
            {{goal_data}}
        
            [사용자 정보]
            {{user_data}}
        
            [요구사항]
            1. 한글 기준 40자에서 50자 이내로 작성
            2. 전문가 톤(~하세요, ~하는 것이 좋습니다)
            3. 가족 기반 치료, 음악/미술 치료, 동기 강화 상담, 점진적 사용 감소, 대체 활동, 자기점검 등 구체적 솔루션 반드시 포함
            4. 구체적 솔루션은 사용자 상황에 적합한 1~2개만 반영
            5. 논문 근거(RAG 컨텍스트)는 요약하여 메시지에 간접 반영 (예: "스트레스가 쌓이면 미술치료로 감정을 표현하며 점진적으로 사용 시간을 줄이는 것이 좋습니다.")
        
            [출력 지침]
            - 최종 메시지 길이는 40~50자의 두세문장
        """)
    @UserMessage("{{rag_query}}")
    String message(
            @V("screen_time_data") String screenTimeData,
            @V("goal_data") String goalData,
            @V("user_data") String userData,
            @V("rag_query") String ragQuery
    );
}
