package com.dm.DM_Backend.domain.account.auth.service;

public interface RefreshTokenService {
    // Refresh Token 저장
    void saveRefreshToken(String memberId, String refreshToken, long duration);

    // Refresh Token 조회
    String getRefreshToken(String memberId);

    // Refresh Token 삭제
    void deleteRefreshToken(String memberId);
}
