package com.dm.dmbackend.domain.account.oauth2.oauth2Web.controller;

import com.dm.dmbackend.domain.account.auth.dto.req.LoginRequest;
import com.dm.dmbackend.domain.account.auth.dto.res.LoginResponse;
import com.dm.dmbackend.domain.account.auth.service.AuthService;
import com.dm.dmbackend.domain.account.member.dto.req.MemberSignUpRequest;
import com.dm.dmbackend.domain.account.member.repository.MemberRepository;
import com.dm.dmbackend.domain.account.member.service.MemberService;
import com.dm.dmbackend.domain.account.oauth2.oauth2Web.service.OAuth2WebService;
import com.dm.dmbackend.domain.account.oauth2.provider.OAuth2Provider;
import com.dm.dmbackend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/rest-api/v1/oauth2/web")
@Tag(name = "OAuth2Web", description = "웹 소셜 로그인 API")
@RequiredArgsConstructor
public class ApiV1OAuth2WebController {
    private final AuthService authService;
    private final OAuth2WebService oAuth2WebService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Value("${frontend.oauth-redirect}")
    private String frontendRedirect;

    // 웹 소셜 로그인 리디렉션 URL
    @GetMapping("/redirect-url/{provider}")
    @Operation(summary = "소셜 로그인 리디렉션 URL")
    public ApiResponse<String> redirectToProvider(@PathVariable("provider") OAuth2Provider provider) {
        String authUrl = oAuth2WebService.getAuthUrl(provider);
        return ApiResponse.success(authUrl);
    }

    // 웹 소셜 로그인
    @GetMapping("/{provider}")
    @Operation(summary = "웹 소셜 로그인", description = "provider={GOOGLE|KAKAO|NAVER}")
    public ResponseEntity<Void> socialLogin(
            @PathVariable("provider") String provider,
            @RequestParam("code") String code) {
        // 소셜 유저 정보 조회
        Map<String, String> socialUser = oAuth2WebService.getUserInfo(provider, code);
        String email = socialUser.get("email");
        String name = socialUser.get("name");

        // 로그인 또는 회원가입 처리
        LoginRequest socialLoginRequest;
        if (memberRepository.existsByEmail(email)) {
            socialLoginRequest = LoginRequest.builder().email(email).build();
        } else {
            MemberSignUpRequest memberSignUpRequest = MemberSignUpRequest.builder()
                    .nickname(null)
                    .job(null)
                    .email(email)
                    .password(null)
                    .motivationType(null)
                    .gender(null)
                    .birthday(null)
                    .build();
            memberService.signup(memberSignUpRequest);
            socialLoginRequest = LoginRequest.builder().email(memberSignUpRequest.getEmail()).build();
        }
        LoginResponse loginResponse = authService.login(socialLoginRequest, true);
        String accessToken = loginResponse.getAccessToken();
        String refreshToken = loginResponse.getRefreshToken();

        // 프론트엔드 리디렉션 주소에 토큰을 쿼리로 포함
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirect)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }
}
