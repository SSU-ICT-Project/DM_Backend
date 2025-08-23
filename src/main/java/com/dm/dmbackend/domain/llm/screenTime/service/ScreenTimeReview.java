package com.dm.dmbackend.domain.llm.screenTime.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ScreenTimeReview {
    @SystemMessage("""
        당신은 사용자의 스크린타임 리뷰어입니다.

        [동기부여 형식]
        {{motivation_prompt}}

        [전날 스크린타임 데이터]
        {{screen_time_data}}

        [사용자의 목표]
        {{goal_data}}

        [사용자 정보]
        {{user_data}}

        [참고 문헌 기반 컨텍스트]
        {{information}}

        요구사항:
        - {{motivation_prompt}} 에 맞게 70자 내외 한국어 메시지를 작성하세요.
        - 디지털 과사용(숏폼 중독, 습관적 SNS 사용)으로 인해 자기개발, 목표 달성에 지장이 생기는 사람들을 위해
          {{motivation_prompt}} 에 맞춰서 {{screen_time_data}}, {{goal_data}}, {{user_data}},
          {{information}} 를 참고해 전날 스크린타임 데이터에 대한 리뷰 메시지를 제공하세요.
        """)
    @UserMessage("""
        [전날 스크린타임 데이터]
        {{screen_time_data}}

        [사용자의 목표]
        {{goal_data}}

        [사용자 정보]
        {{user_data}}

        위 정보를 반영해 리뷰 메시지를 생성해 주세요.
        """)
    String message(
            @V("motivation_prompt") String motivationPrompt,
            @V("screen_time_data") String screenTimeData,
            @V("goal_data") String goalData,
            @V("user_data") String userData
    );
}
