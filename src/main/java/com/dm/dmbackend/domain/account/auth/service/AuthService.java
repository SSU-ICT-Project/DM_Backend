package com.dm.dmbackend.domain.account.auth.service;

import com.dm.dmbackend.domain.account.auth.dto.req.LoginRequest;
import com.dm.dmbackend.domain.account.auth.dto.res.LoginResponse;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;

public interface AuthService {
    // 로그인
    LoginResponse login(LoginRequest loginRequest, boolean isSocialLogin);

    // 로그아웃
    void logout(LoginUserDto loginUser, String accessToken);

    // accessToken 재발급
    LoginResponse refreshToken(String refreshToken);
}
