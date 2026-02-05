package com.dm.dmbackend.domain.account.auth.controller;

import com.dm.dmbackend.domain.account.auth.dto.req.LoginRequest;
import com.dm.dmbackend.domain.account.auth.dto.req.RefreshTokenRequest;
import com.dm.dmbackend.domain.account.auth.dto.res.LoginResponse;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUser;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.auth.service.AuthService;
import com.dm.dmbackend.global.common.response.ApiResponse;
import com.dm.dmbackend.global.security.TokenResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest-api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증/인가 API")
public class AuthController {
    private final AuthService authService;
    private final TokenResolver tokenResolver;

    // 로그인
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "계정ID와 비밀번호로 로그인합니다.")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ApiResponse.success(authService.login(loginRequest, false));
    }

    // 로그아웃
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자가 로그아웃합니다.")
    public ApiResponse<Void> logout(HttpServletRequest request, @LoginUser LoginUserDto loginUser) {
        String accessToken = tokenResolver.resolveAccess(request);
        authService.logout(loginUser, accessToken);
        return ApiResponse.success();
    }

    // AccessToken 재발급
    @Operation(summary = "AccessToken 재발급", description = "Refresh 토큰을 사용하여 Access 토큰을 재발급합니다.")
    @PostMapping("/refresh-token")
    public ApiResponse<LoginResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        return ApiResponse.success(authService.refreshToken(refreshTokenRequest.getRefreshToken()));
    }
}
