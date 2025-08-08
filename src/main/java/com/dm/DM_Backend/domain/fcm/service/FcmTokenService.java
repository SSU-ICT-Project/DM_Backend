package com.dm.DM_Backend.domain.fcm.service;

public interface FcmTokenService {
    // FCM Token 조회
    String getFcmToken(Long memberId);

    // FCM Token 저장
    void saveFcmToken(Long memberId, String fcmToken);

    // FCM Token 삭제
    void deleteFcmToken(Long memberId);
}
