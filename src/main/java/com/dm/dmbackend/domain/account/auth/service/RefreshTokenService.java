package com.dm.dmbackend.domain.account.auth.service;

import java.time.Duration;

public interface RefreshTokenService {
    // Refresh Token 저장
    void saveRefreshToken(String memberId, String refreshToken, Duration duration);

    // Refresh Token 조회
    String getRefreshToken(String memberId);

    // Refresh Token 삭제
    void deleteRefreshToken(String memberId);
}
