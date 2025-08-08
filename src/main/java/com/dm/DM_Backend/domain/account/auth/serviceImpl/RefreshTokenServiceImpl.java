package com.dm.DM_Backend.domain.account.auth.serviceImpl;

import com.dm.DM_Backend.domain.account.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RedisTemplate<String, Object> redisTemplate;

    // Refresh Token 저장
    @Override
    @Transactional
    public void saveRefreshToken(String memberId, String refreshToken, long duration) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(memberId, refreshToken, duration, TimeUnit.MILLISECONDS);
    }

    // Refresh Token 조회
    @Override
    @Transactional
    public String getRefreshToken(String memberId) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        Object value = ops.get(memberId);
        return value != null ? value.toString() : null;
    }

    // Refresh Token 삭제
    @Override
    @Transactional
    public void deleteRefreshToken(String memberId) {
        redisTemplate.delete(memberId);
    }
}
