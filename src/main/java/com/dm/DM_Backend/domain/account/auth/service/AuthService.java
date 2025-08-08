package com.dm.DM_Backend.domain.account.auth.service;

import com.dm.DM_Backend.domain.account.auth.dto.req.LoginForm;
import com.dm.DM_Backend.domain.account.auth.dto.res.Auth;
import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;

public interface AuthService {
    // 로그인
    Auth login(LoginForm loginForm, boolean isSocialLogin);

    // 로그아웃
    void logout(LoginUserDto loginUser);

    // accessToken 재발급
    Auth refreshToken(String refreshToken, LoginUserDto loginUser);
}
