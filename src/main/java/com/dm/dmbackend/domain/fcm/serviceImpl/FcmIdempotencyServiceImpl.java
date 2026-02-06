package com.dm.dmbackend.domain.fcm.serviceImpl;

import com.dm.dmbackend.domain.fcm.service.FcmIdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmIdempotencyServiceImpl implements FcmIdempotencyService {
    private static final String KEY_PREFIX = "fcm:dedup:";
    private static final Duration PROCESSING_TTL = Duration.ofMinutes(3);
    private static final Duration SENT_TTL = Duration.ofDays(2);
    private final StringRedisTemplate redisTemplate;

    // 처음 처리하는 eventId면 true, 이미 처리(또는 처리중)이면 false
    public boolean tryAcquire(String eventId) {
        String key = KEY_PREFIX + eventId;
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, "PROCESSING", PROCESSING_TTL);
        return Boolean.TRUE.equals(ok);
    }

    // 처리 성공 표시: TTL 연장하거나 상태 바꿀 때 사용
    public void markSuccess(String eventId) {
        String key = KEY_PREFIX + eventId;
        redisTemplate.opsForValue().set(key, "SENT", SENT_TTL);
    }

    // 처리 실패 시: 재시도를 위해 락 해제
    public void release(String eventId) {
        String key = KEY_PREFIX + eventId;
        redisTemplate.delete(key);
    }
}
