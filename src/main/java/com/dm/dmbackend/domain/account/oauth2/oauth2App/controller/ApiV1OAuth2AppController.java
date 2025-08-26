package com.dm.dmbackend.domain.account.oauth2.oauth2App.controller;

import com.dm.dmbackend.domain.account.auth.dto.req.LoginForm;
import com.dm.dmbackend.domain.account.auth.dto.res.Auth;
import com.dm.dmbackend.domain.account.auth.service.AuthService;
import com.dm.dmbackend.domain.account.member.dto.req.MemberForm;
import com.dm.dmbackend.domain.account.member.repository.MemberRepository;
import com.dm.dmbackend.domain.account.member.service.MemberService;
import com.dm.dmbackend.domain.account.oauth2.oauth2App.dto.req.OAuth2AppLoginRequest;
import com.dm.dmbackend.domain.account.oauth2.oauth2App.dto.res.OAuth2AppTokensResponse;
import com.dm.dmbackend.domain.account.oauth2.oauth2App.service.OAuth2AppService;
import com.dm.dmbackend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest-api/v1/oauth2/app")
@Tag(name = "OAuth2App", description = "앱 소셜 로그인 API")
@RequiredArgsConstructor
public class ApiV1OAuth2AppController {
    private final OAuth2AppService oAuth2AppService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final AuthService authService;

    @PostMapping("/{provider}")
    @Operation(summary = "앱 소셜 로그인", description = "provider={google|kakao|naver}")
    @Transactional
    public ApiResponse<OAuth2AppTokensResponse> appLogin(
            @PathVariable("provider") String provider,
            @RequestBody OAuth2AppLoginRequest req) {

        // 1) 토큰 검증 → 가입 후보 MemberForm 산출
        MemberForm candidate = oAuth2AppService.toMemberForm(provider, req);

        // 2) 이메일 기준으로 가입 여부 판단 (미가입이면 가입)
        boolean isNew = false;
        if (!memberRepository.existsByEmail(candidate.getEmail())) {
            memberService.signup(candidate);
            isNew = true;
        }
        // 3) 기존 앱 로그인(JWT 발급) — 소셜 로그인 플래그로 비번검증 생략
        Auth auth = authService.login(
                LoginForm.builder().email(candidate.getEmail()).build(),
                true
        );
        return ApiResponse.of(new OAuth2AppTokensResponse(
                auth.getAccessToken(),
                auth.getRefreshToken()
        ));
    }
}
