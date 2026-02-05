package com.dm.dmbackend.global.config;

import com.dm.dmbackend.domain.llm.digitalDetox.service.DigitalDetoxCure;
import com.dm.dmbackend.domain.llm.digitalDetox.service.DigitalDetoxMotivate;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmConfig {
    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String openAiApiKey;

    @Value("${vectorstore.pg.tableName}")
    private String tableName;

    @Value("${vectorstore.pg.dimension}") // text-embedding-3-small → 1536
    private int dimension;

    // 추가: PG 접속 정보
    @Value("${vectorstore.pg.host}")
    private String pgHost;

    @Value("${vectorstore.pg.port}")
    private int pgPort;

    @Value("${vectorstore.pg.database}")
    private String pgDatabase;

    @Value("${vectorstore.pg.user}")
    private String pgUser;

    @Value("${vectorstore.pg.password}")
    private String pgPassword;

    // OpenAI Embedding 모델
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(openAiApiKey)
                .modelName("text-embedding-3-small")
                .build();
    }

    // PgVector Embedding Store (host/port 방식)
    @Bean
    public PgVectorEmbeddingStore pgVectorStore() {
        return PgVectorEmbeddingStore.builder()
                .host(pgHost)
                .port(pgPort)
                .database(pgDatabase)
                .user(pgUser)
                .password(pgPassword)
                .table(tableName)
                .dimension(dimension)
                .createTable(true)      // 필요시 테이블 자동 생성
                .build();
    }

    // RAG Retriever
    @Bean
    public EmbeddingStoreContentRetriever retriever(
            PgVectorEmbeddingStore pgVectorStore,
            EmbeddingModel embeddingModel
    ) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(pgVectorStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)      // 필요에 맞게 조정
                .minScore(0.6)      // 필요에 맞게 조정
                .build();
    }

    // 자동 RAG 채워 넣는 augmentor
    @Bean
    public RetrievalAugmentor digitalDetoxAugmentor(EmbeddingStoreContentRetriever retriever) {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(retriever)
                .build();
    }

    // 자동 RAG 연결됨
    @Bean
    public DigitalDetoxCure digitalDetoxCure(ChatModel chatModel,
                                             RetrievalAugmentor digitalDetoxAugmentor) {
        return AiServices.builder(DigitalDetoxCure.class)
                .chatModel(chatModel)
                .retrievalAugmentor(digitalDetoxAugmentor)
                .build();
    }

    // RAG 사용X
    @Bean
    public DigitalDetoxMotivate digitalDetoxMotivate(ChatModel chatModel) {
        return AiServices.builder(DigitalDetoxMotivate.class)
                .chatModel(chatModel)
                .build();
    }
}
