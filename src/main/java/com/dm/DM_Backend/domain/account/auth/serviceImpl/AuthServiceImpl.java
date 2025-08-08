package com.dm.DM_Backend.domain.account.auth.serviceImpl;

import com.dm.DM_Backend.domain.account.auth.dto.req.LoginForm;
import com.dm.DM_Backend.domain.account.auth.dto.res.Auth;
import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.account.auth.security.JwtTokenProvider;
import com.dm.DM_Backend.domain.account.auth.service.AuthService;
import com.dm.DM_Backend.domain.account.auth.service.RefreshTokenService;
import com.dm.DM_Backend.domain.account.member.entity.Member;
import com.dm.DM_Backend.domain.account.member.repository.MemberRepository;
import com.dm.DM_Backend.global.exception.ReturnCode;
import com.dm.DM_Backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Value("${custom.accessToken.expiration}")
    private long accessTokenExpiration;

    @Value("${custom.refreshToken.expiration}")
    private long refreshTokenExpiration;

    // 로그인
    @Override
    @Transactional
    public Auth login(LoginForm loginForm, boolean isSocialLogin) {
        Member member = memberRepository.findByEmail(loginForm.getEmail())
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        // 소셜 로그인이라면 비밀번호 검증을 생략
        if (!isSocialLogin && !passwordEncoder.matches(loginForm.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }
        String accessToken = jwtTokenProvider.generateToken(member.getEmail(), accessTokenExpiration);
        String refreshToken = jwtTokenProvider.generateToken(member.getEmail(), refreshTokenExpiration);
        // Refresh Token을 Redis에 저장
        refreshTokenService.saveRefreshToken(member.getId().toString(), refreshToken, refreshTokenExpiration);
        return new Auth(accessToken, refreshToken);
    }

    // 로그아웃
    @Override
    @Transactional
    public void logout(LoginUserDto loginUser) {
        // Redis에서 Refresh Token 삭제
        refreshTokenService.deleteRefreshToken(loginUser.getId().toString());
    }

    // accessToken 재발급
    @Override
    @Transactional
    public Auth refreshToken(String refreshToken, LoginUserDto loginUser) {
        // "Bearer "가 붙어있다면 제거
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }

        String storedRefreshToken = refreshTokenService.getRefreshToken(loginUser.getId().toString());
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }
        if (!jwtTokenProvider.validateToken(storedRefreshToken)) {
            throw new RuntimeException("리프레시 토큰이 만료되었습니다.");
        }
        String email = jwtTokenProvider.getEmailFromToken(storedRefreshToken);
        String newAccessToken = jwtTokenProvider.generateToken(email, accessTokenExpiration);

        return new Auth(newAccessToken, storedRefreshToken);
    }
}
