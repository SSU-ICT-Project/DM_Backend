package com.dm.DM_Backend.domain.llm.ingest.service;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;

public interface IngestService {
    // 논문 임베딩
    void ingestAll(LoginUserDto loginUserDto);

    // 임베딩된 논문 개수 조회
    long getEmbeddingCount(LoginUserDto loginUserDto);

    // 임베딩된 논문 전체 삭제
    void clearAll(LoginUserDto loginUserDto);
}
