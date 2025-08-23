package com.dm.DM_Backend.domain.llm.screenTime.serviceImpl;

import dev.langchain4j.model.chat.ChatModel; // 'ChatLanguageModel' 대신 'ChatModel'을 import 합니다.
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchSnippet;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetGoalGuideImpl {

    // 현재 버전에 맞춰 'ChatModel'을 사용합니다.
    private final ChatModel chatModel;
    private final WebSearchEngine webSearchEngine;

    /**
     * 사용자의 목표(goalData)를 입력받아 웹 검색을 기반으로 목표 달성 가이드를 생성합니다.
     * @param goalData 사용자가 설정한 목표 문자열 (예: "토익 950점 달성")
     * @return LLM이 생성한 목표 달성 가이드 문자열
     */
    @Transactional
    public String getGoalGuide(String goalData) {

        // 1. 웹에서 사용자의 목표와 관련된 최신 정보 검색
        List<WebSearchSnippet> searchResults = webSearchEngine.search(goalData, 10);

        // 2. 검색 결과를 LLM이 이해하기 쉬운 하나의 긴 텍스트(context)로 가공
        String context = searchResults.stream()
                .map(WebSearchSnippet::text)
                .collect(Collectors.joining("\n\n---\n\n"));

        // 3. LLM에게 보낼 지시서 (프롬프트 템플릿) 정의
        PromptTemplate promptTemplate = PromptTemplate.from(
                """
                당신은 사용자의 목표 달성을 돕는 전문 동기부여 코치입니다.
                아래 제공되는 웹 검색 결과를 바탕으로 사용자의 목표인 '{{goal}}'에 대한 구체적인 가이드를 작성해주세요.
                
                다음 세 가지 항목을 반드시 포함하여 명확하고 친절한 어조로 설명해야 합니다:
                
                ### 1. 목표의 의미와 중요성
                (예: 토익 950점은 왜 중요하며, 전체 응시자 중 상위 몇 퍼센트에 해당하는지와 같은 객관적인 정보)
                
                ### 2. 목표 달성을 위한 과정
                (목표를 이루기 위해 거쳐야 할 단계적인 학습 또는 준비 과정)
                
                ### 3. 구체적인 실천 행동
                (오늘 당장 시작할 수 있는 구체적이고 실질적인 행동들)
                
                ---
                [웹 검색 결과 참고자료]
                {{context}}
                ---
                
                위 내용을 바탕으로 최종 답변은 반드시 한글로 작성해주세요.
                """
        );

        // 4. 프롬프트 템플릿에 실제 데이터(사용자 목표, 검색 결과)를 채워넣기
        Map<String, Object> variables = Map.of(
                "goal", goalData,
                "context", context
        );
        Prompt prompt = promptTemplate.apply(variables);

        // 5. 완성된 프롬프트를 LLM(ChatModel)에 보내고, 생성된 답변을 반환
        // 이전 버전에서는 바로 문자열을 반환하는 경우가 많습니다.
        return chatModel.generate(prompt.text());
    }
}