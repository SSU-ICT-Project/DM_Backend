package com.dm.dmbackend.domain.account.oauth2.oauth2App.controller;

import com.dm.dmbackend.domain.account.oauth2.oauth2App.dto.req.OAuth2AppLoginRequest;
import com.dm.dmbackend.domain.account.oauth2.oauth2App.dto.res.OAuth2AppTokensResponse;
import com.dm.dmbackend.global.common.enums.OAuth2Provider;
import com.dm.dmbackend.domain.account.oauth2.oauth2App.service.OAuth2AppService;
import com.dm.dmbackend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest-api/v1/oauth2/app")
@Tag(name = "OAuth2App", description = "앱 소셜 로그인 API")
@RequiredArgsConstructor
public class ApiV1OAuth2AppController {
    private final OAuth2AppService oAuth2AppService;

    // 앱 소셜 로그인(회원가입)
    @PostMapping("/{provider}")
    @Operation(summary = "앱 소셜 로그인", description = "provider={GOOGLE|KAKAO|NAVER}")
    public ApiResponse<OAuth2AppTokensResponse> appLogin(
            @PathVariable("provider") OAuth2Provider provider,
            @RequestBody @Valid OAuth2AppLoginRequest req
    ) {
        OAuth2AppTokensResponse tokens = oAuth2AppService.appLogin(provider, req);
        return ApiResponse.success(tokens);
    }
}
