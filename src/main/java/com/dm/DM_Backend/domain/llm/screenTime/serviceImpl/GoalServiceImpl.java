package com.dm.DM_Backend.domain.llm.screenTime.serviceImpl;

public class GoalServiceImpl {
    @Transactional
    public String generateGoalReview(String goalInput, Member.MotivationType userType) {
        String motivationStrategy = mapMotivationPrompt(userType);

        List<Content> contents = retriever.retrieve(Query.from(goalInput));
        String context = contents.stream()
                .map(c -> (c instanceof TextSegment ts) ? ts.text() : c.toString())
                .collect(Collectors.joining("\n"));

        PromptTemplate template = PromptTemplate.from("""
                당신은 사용자의 목표 코치입니다.

                [사용자의 목표]
                {{goal_input}}

                [웹 검색 기반 참고 정보]
                {{context}}

                [사용자 성향 정보]
                {{motivation_strategy}}

                아래 항목을 기반으로 사용자를 위한 요약 메시지를 생성하세요:

                1. 이 목표가 왜 중요한지, 사회적 맥락 또는 통계적 의미 설명
                2. 이 목표를 달성하기 위해 필요한 준비 및 중간 과정
                3. 사용자가 오늘 당장 할 수 있는 작고 실현 가능한 행동 제안
                4. 사용자 성향에 맞는 톤과 방식으로 동기부여 메시지 작성

                최종 출력:
                - 약 200자 이내 한국어 메시지
                - 정보 기반 + 맞춤형 동기부여를 함께 담을 것
                - 너무 길거나 딱딱하지 않도록 자연스럽고 실천 중심으로 작성
                """
        );

        String prompt = template.apply(Map.of(
                "goal_input", goalInput,
                "context", context,
                "motivation_strategy", motivationStrategy
        )).text();

        return chatModel.chat(prompt);
    }
}
