package com.dm.dmbackend.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {
    private final SecretKey key;

    // SecretKey 초기화 (한 번만 생성)
    public JwtTokenProvider(@Value("${custom.jwt.secretKey}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT 생성
    public String generateToken(String email, String type, Duration expiration) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .id(UUID.randomUUID().toString())
                .claim("tokenType", type)
                .issuedAt(new Date())
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key, Jwts.SIG.HS256)  // 필드 key 사용
                .compact();
    }

    // ACCESS Token 검증
    public boolean validateAccessToken(String token) {
        return validateToken(token, "ACCESS");
    }

    // REFRESH Token 검증
    public boolean validateRefreshToken(String token) {
        return validateToken(token, "REFRESH");
    }

    // JWT 검증
    private boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("tokenType", String.class);
            if (!expectedType.equals(tokenType)) {
                log.warn("토큰 타입 불일치: expected={}, actual={}", expectedType, tokenType);
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("토큰이 만료되었습니다: {}", e.getMessage());
        }
        return false;
    }

    // 이메일 추출
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // jti/TTL 헬퍼
    public String getJti(String token) {
        return parse(token).getId(); // 표준 jti
    }

    public long getRemainingSeconds(String token) {
        Date exp = parse(token).getExpiration();
        long diffMs = exp.getTime() - System.currentTimeMillis();
        return Math.max(0, diffMs / 1000);
    }

    // 공통 파서
    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
