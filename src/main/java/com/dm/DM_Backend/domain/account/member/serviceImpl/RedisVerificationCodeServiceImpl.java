package com.dm.DM_Backend.domain.account.member.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class RedisVerificationCodeServiceImpl {
    private final RedisTemplate<String, Integer> integerRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${mail.code-expiration-millis}")
    int duration;

    //이메일 인증 코드 저장
    public void saveCodeByEmail(String email, Integer code) {
        integerRedisTemplate.opsForValue().set("code:" + email, code, duration, TimeUnit.MILLISECONDS);
    }

    //이메일 인증 코드 반환
    public Integer getCodeByEmail(String email) {
        return integerRedisTemplate.opsForValue().get("code:" + email);
    }

    //이메일 인증 코드 삭제
    public void deleteCodeByEmail(String email) {
        integerRedisTemplate.delete("code:" + email);
    }


    //비밀번호 재설정용 토큰 생성및 반환
    public String generateAndStoreResetToken(String email) {
        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("reset:" + email, resetToken, Duration.ofMinutes(10));
        return resetToken;
    }

    //비밀번호 재설정용 토큰 확인
    public Boolean checkResetToken(String email, String token) {
        String resetToken = redisTemplate.opsForValue().get("reset:" + email).toString();
        if(token.equals(resetToken)) {
            redisTemplate.delete("reset:" + email);
            return true;
        }
        return false;
    }

    public void deleteResetToken(String email) {
        redisTemplate.delete("reset:" + email);
    }
}
