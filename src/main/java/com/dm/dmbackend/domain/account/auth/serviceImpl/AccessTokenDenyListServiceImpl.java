package com.dm.dmbackend.domain.account.auth.serviceImpl;

import com.dm.dmbackend.domain.account.auth.service.AccessTokenDenyListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenDenyListServiceImpl implements AccessTokenDenyListService {
    private static final String KEY_PREFIX = "jwt:deny:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void deny(String jti, Duration ttl) {
        if (jti == null || jti.isBlank()) return;
        if (ttl == null || ttl.isZero() || ttl.isNegative()) return;
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", ttl);
    }

    @Override
    public boolean isDenied(String jti) {
        if (jti == null || jti.isBlank()) return false;
        Boolean exists = stringRedisTemplate.hasKey(KEY_PREFIX + jti);
        return Boolean.TRUE.equals(exists);
    }
}
