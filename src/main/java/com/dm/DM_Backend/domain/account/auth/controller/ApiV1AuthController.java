package com.dm.DM_Backend.domain.account.auth.controller;

import com.dm.DM_Backend.domain.account.auth.dto.req.LoginForm;
import com.dm.DM_Backend.domain.account.auth.dto.res.Auth;
import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUser;
import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.account.auth.service.AuthService;
import com.dm.DM_Backend.global.common.response.ApiResponse;
import com.dm.DM_Backend.global.exception.ReturnCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest-api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증/인가 API")
public class ApiV1AuthController {
    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "계정ID와 비밀번호로 로그인합니다.")
    public ApiResponse<Auth> login(@RequestBody @Valid LoginForm loginForm) {
        return ApiResponse.of(authService.login(loginForm, false));
    }

    // 로그아웃
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자가 로그아웃합니다.")
    public ApiResponse<String> logout(@LoginUser LoginUserDto loginUser) {
        authService.logout(loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // accessToken 재발급
    @Operation(summary = "accessToken 재발급", description = "refresh 토큰을 사용하여 access 토큰을 재발급합니다.")
    @GetMapping("/refresh-token")
    public ApiResponse<Auth> refreshToken(@RequestHeader("Authorization") String refreshToken, @LoginUser LoginUserDto loginUser) {
        return ApiResponse.of(authService.refreshToken(refreshToken, loginUser));
    }
}
