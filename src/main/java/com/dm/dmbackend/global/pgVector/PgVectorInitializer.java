package com.dm.dmbackend.global.pgVector;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PgVectorInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        log.info("Checking if pgvector extension and embeddings table exist...");

        // 1) pgvector extension 설치 (없으면 추가)
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");

        // 2) 테이블 생성 (없으면 추가)
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS screen_time_embeddings (
                id bigserial PRIMARY KEY,
                content text,
                embedding vector(1536)
            )
        """);

        log.info("pgvector table initialized successfully (if not exists).");
    }
}
