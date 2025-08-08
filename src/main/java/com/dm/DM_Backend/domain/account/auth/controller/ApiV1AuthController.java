package com.dm.DM_Backend.domain.account.auth.controller;

import com.dm.DM_Backend.domain.account.auth.dto.req.LoginForm;
import com.dm.DM_Backend.domain.account.auth.dto.res.Auth;
import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUser;
import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.account.auth.service.AuthService;
import com.dm.DM_Backend.global.common.response.ApiResponse;
import com.dm.DM_Backend.global.exception.ReturnCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest-api/v1/auth")
@RequiredArgsConstructor
public class ApiV1AuthController {
    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    public ApiResponse<Auth> login(@RequestBody @Valid LoginForm loginForm) {
        return ApiResponse.of(authService.login(loginForm, false));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ApiResponse<String> logout(@LoginUser LoginUserDto loginUser) {
        authService.logout(loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // accessToken 재발급
    @GetMapping("/refresh-token")
    public ApiResponse<Auth> refreshToken(@RequestHeader("Authorization") String refreshToken, @LoginUser LoginUserDto loginUser) {
        return ApiResponse.of(authService.refreshToken(refreshToken, loginUser));
    }
}
