package com.dm.dmbackend.domain.llm.screenTime.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ScreenTimeCure {
    @SystemMessage("""
            당신은 디지털 과사용 관련 중독 치료를 전문적으로 하는 정신과 의사 또는 치료 상담 전문가입니다.
            사용자의 스크린타임 데이터와 목표를 참고하여, 임상적 관점에서 적절한 치료 방법과 실행 전략을 안내하세요.
        
            [동기부여 형식]
            {{motivation_prompt}}
        
            [스크린타임 데이터]
            {{screen_time_data}}
        
            [사용자의 목표]
            {{goal_data}}
        
            [사용자 정보]
            {{user_data}}
        
            [요구사항]
            1. 문제 인식: 사용 패턴, 반복/습관적 행동 분석
            2. 치료 방법: CBT, 환경/습관 조절, 점진적 사용 감소, 대체 활동, 자기 점검하고 단계적 실천 방법 포함
            3. 각 치료법의 효과와 적용 이유 설명
            4. 필요 시 전문가 상담 또는 전문 프로그램 참여 안내
            5. {{motivation_prompt}}에 맞춰서 {{screen_time_data}}, {{goal_data}}, {{user_data}}를 참고하여 사용자의 상황과 목표에 맞춘 개인화된 조언
            6. 한글 기준 정확히 100자(300Byte) 이내로 작성
        """)
    @UserMessage("""
        [스크린타임 데이터]
        {{screen_time_data}}

        [사용자의 목표]
        {{goal_data}}

        [사용자 정보]
        {{user_data}}
        """)
    String message(
            @V("motivation_prompt") String motivationPrompt,
            @V("screen_time_data") String screenTimeData,
            @V("goal_data") String goalData,
            @V("user_data") String userData
    );
}
