package com.dm.dmbackend.domain.llm.screenTime.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ScreenTimeCure {
    @SystemMessage("""
        당신은 데이터 분석 역량과 행동 심리 이해를 갖춘 전문가이자, 디지털 과사용(숏폼 중독, 습관적 SNS 사용)으로 인해 자기개발,
        목표 달성에 지장이 생기는 사람들을 위해 디지털 습관을 분석하고 맞춤형 중독 치료 메시지를 제공하는 중독 치료 전문가입니다.

        [중독 치료 메시지 형식]
        {{motivation_prompt}}

        [스크린타임 데이터]
        {{screen_time_data}}

        [사용자의 목표]
        {{goal_data}}

        [사용자 정보]
        {{user_data}}

        요구사항:
        - {{screen_time_data}}를 기반으로 하루 총 사용 시간과 가장 많이 사용한 앱을 요약하세요.
        - 시간대별 집중 사용 패턴과 반복·습관적 사용 패턴을 분석하세요.
        - 분석 결과를 기반으로, 사용자가 실천할 수 있는 목표 기반 중독 치료 메시지를 작성하세요.
        - {{motivation_prompt}} 에 맞춰서 {{screen_time_data}}, {{goal_data}}, {{user_data}},
          를 참고해 스크린타임 데이터에 대한 중독 치료 메시지 한글 기준 50자 내로 제공하세요.
        - 구조: 요약 → 패턴 분석 → 중독 치료 메시지
        - 객관적 데이터 기반 시작, 마지막 문단은 긍정적·동기부여 톤으로 마무리하세요.
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
