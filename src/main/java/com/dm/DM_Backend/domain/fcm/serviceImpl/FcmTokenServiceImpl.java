package com.dm.DM_Backend.domain.fcm.serviceImpl;

import com.dm.DM_Backend.domain.fcm.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenServiceImpl implements FcmTokenService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String FCM_TOKEN_PREFIX = "fcm:"; // Redis key prefix

    @Value("${custom.refreshToken.expiration}")
    private long refreshTokenExpiration; // FCM 토큰 만료 시간

    // FCM Token 조회
    @Override
    @Transactional
    public String getFcmToken(Long memberId) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        String key = getFcmTokenKey(memberId);
        Object value = ops.get(key);
        return value != null ? value.toString() : null;
    }

    // FCM Token 저장
    @Override
    @Transactional
    public void saveFcmToken(Long memberId, String fcmToken) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        String key = getFcmTokenKey(memberId);
        ops.set(key, fcmToken, refreshTokenExpiration, TimeUnit.MILLISECONDS);
    }

    // FCM Token 삭제
    @Override
    @Transactional
    public void deleteFcmToken(Long memberId) {
        String key = getFcmTokenKey(memberId);
        redisTemplate.delete(key);
    }

    private String getFcmTokenKey(Long memberId) {
        return FCM_TOKEN_PREFIX + memberId; // e.g., fcm:123
    }
}
