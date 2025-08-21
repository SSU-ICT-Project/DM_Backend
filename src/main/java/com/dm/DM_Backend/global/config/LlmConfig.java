package com.dm.DM_Backend.global.config;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.nio.file.Path;

@Configuration
public class LlmConfig {
    @Value("${spring.rag.ingest}")
    private boolean ingestEnabled;

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
                .maxResults(3)
                .minScore(0.7)
                .build();
    }

    @Bean
    @DependsOn("pgVectorStore")
    public CommandLineRunner ingestRunner(EmbeddingModel embeddingModel,
                                          PgVectorEmbeddingStore store) {
        return args -> {
            if (!ingestEnabled) {
                return; // 아무 것도 하지 않음
            }
            DocumentSplitter splitter = DocumentSplitters.recursive(300, 50);
            var ingestor = EmbeddingStoreIngestor.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(store)
                    .documentSplitter(splitter)
                    .build();
            var doc = FileSystemDocumentLoader.loadDocument(
                    Path.of("docs/paper1.pdf"),
                    new ApachePdfBoxDocumentParser()
            );
            ingestor.ingest(doc);
        };
    }
}
