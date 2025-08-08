package com.dm.DM_Backend.domain.account.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${custom.jwt.secretKey}")
    private String secretKey;

    private final SecretKey key;

    // SecretKey 초기화 (한 번만 생성)
    public JwtTokenProvider(@Value("${custom.jwt.secretKey}") String secretKey) {
        this.secretKey = secretKey;
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT 생성
    public String generateToken(String email, long expiration) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, Jwts.SIG.HS256)  // 필드 key 사용
                .compact();
    }

    // JWT 디코딩
    public void decodeToken(String token) {
        Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    // JWT 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
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

    // 요청 헤더에서 JWT 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
