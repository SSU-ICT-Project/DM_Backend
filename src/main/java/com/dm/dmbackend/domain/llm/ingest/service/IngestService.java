package com.dm.dmbackend.domain.llm.ingest.service;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;

public interface IngestService {
    // 논문 임베딩
    void ingestAll(LoginUserDto loginUserDto);

    // 임베딩된 논문의 청크 개수 조회
    long getEmbeddingCount(LoginUserDto loginUserDto);

    // 임베딩된 논문 전체 삭제
    void clearAll(LoginUserDto loginUserDto);
}
