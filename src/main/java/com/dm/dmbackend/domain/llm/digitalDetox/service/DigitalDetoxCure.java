package com.dm.dmbackend.domain.llm.digitalDetox.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface DigitalDetoxCure {
    @SystemMessage("""
            당신은 디지털 과사용 중독 치료 전문가입니다.
            사용자의 데이터와 목표를 참고하여 구체적이고 명확한 맞춤형 메시지를 생성하세요.
            최종 출력은 한국어 문장(공백포함 40~50자)으로 작성하세요.
        
            [스크린타임 데이터]
            {{screen_time_data}}
        
            [사용자의 목표]
            {{goal_data}}
        
            [사용자 정보]
            {{user_data}}
        
            [요구사항]
            1. 최종 메시지는 공백 포함 40~50자
            2. 전문가 톤 유지 (~하세요, ~하는 것이 좋습니다)
            3. 메시지 구조: "문제 인식 + 개선 행동 제안" 형식으로 작성
            4. 논문 근거(RAG 컨텍스트)는 요약하여 메시지에 간접 반영(예: 미술치료를 통해 사용 시간을 줄여보세요.")
            5. 목표에 맞춰 사용자마다 매번 다른 솔루션을 제시하세요
            6. 메시지 내용이 자연스럽고 의미가 통하게 작성하세요
        
            [출력 지침]
            - 최종 메시지 길이는 공백포함 40~50자
        """)
    @UserMessage("{{rag_query}}")
    String message(
            @V("screen_time_data") String screenTimeData,
            @V("goal_data") String goalData,
            @V("user_data") String userData,
            @V("rag_query") String ragQuery
    );
}
