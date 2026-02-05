package com.dm.dmbackend.domain.account.auth.serviceImpl;

import com.dm.dmbackend.domain.account.auth.dto.req.LoginRequest;
import com.dm.dmbackend.domain.account.auth.dto.res.LoginResponse;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.auth.service.AccessTokenDenyListService;
import com.dm.dmbackend.domain.account.auth.service.AuthService;
import com.dm.dmbackend.domain.account.auth.service.RefreshTokenService;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.repository.MemberRepository;
import com.dm.dmbackend.global.exception.ReturnCode;
import com.dm.dmbackend.global.exception.ServiceException;
import com.dm.dmbackend.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenDenyListService accessTokenDenyListService;

    @Value("${custom.accessToken.expiration}")
    private Duration accessTokenExpiration;

    @Value("${custom.refreshToken.expiration}")
    private Duration refreshTokenExpiration;

    // 로그인
    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest, boolean isSocialLogin) {
        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        // 소셜 로그인이라면 비밀번호 검증을 생략
        if (!isSocialLogin && !passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }
        String accessToken = jwtTokenProvider.generateToken(member.getEmail(), "ACCESS", accessTokenExpiration);
        String refreshToken = jwtTokenProvider.generateToken(member.getEmail(), "REFRESH", refreshTokenExpiration);
        // Refresh Token을 Redis에 저장
        refreshTokenService.saveRefreshToken(member.getEmail(), refreshToken, refreshTokenExpiration);
        return new LoginResponse(accessToken, refreshToken);
    }

    // 로그아웃
    @Override
    public void logout(LoginUserDto loginUser, String accessToken) {
        // Redis에서 Refresh Token 삭제
        refreshTokenService.deleteRefreshToken(loginUser.getId().toString());
        // access 즉시 차단(deny-list)
        if (accessToken != null && jwtTokenProvider.validateAccessToken(accessToken)) {
            String jti = jwtTokenProvider.getJti(accessToken);
            long ttlSec = jwtTokenProvider.getRemainingSeconds(accessToken);
            if (ttlSec > 0) {
                accessTokenDenyListService.deny(jti, Duration.ofSeconds(ttlSec));
            }
        }
    }

    // accessToken 재발급
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("리프레시 토큰이 없습니다.");
        }
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }
        // 1) 서명/만료 검증 (여기서 만료면 바로 컷)
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("리프레시 토큰이 만료되었거나 유효하지 않습니다.");
        }
        // 2) refreshToken에서 사용자 식별자 추출
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String storedRefreshToken = refreshTokenService.getRefreshToken(email);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }
        if (!jwtTokenProvider.validateRefreshToken(storedRefreshToken)) {
            throw new RuntimeException("리프레시 토큰이 만료되었습니다.");
        }
        String newAccessToken = jwtTokenProvider.generateToken(email, "ACCESS", accessTokenExpiration);
        return new LoginResponse(newAccessToken, storedRefreshToken);
    }
}
