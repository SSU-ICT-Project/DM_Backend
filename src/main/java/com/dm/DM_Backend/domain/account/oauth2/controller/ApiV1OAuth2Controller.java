package com.dm.DM_Backend.domain.account.oauth2.controller;

import com.dm.DM_Backend.domain.account.auth.dto.req.LoginForm;
import com.dm.DM_Backend.domain.account.auth.dto.res.Auth;
import com.dm.DM_Backend.domain.account.auth.service.AuthService;
import com.dm.DM_Backend.domain.account.member.dto.req.MemberForm;
import com.dm.DM_Backend.domain.account.member.entity.Member;
import com.dm.DM_Backend.domain.account.member.repository.MemberRepository;
import com.dm.DM_Backend.domain.account.member.service.MemberService;
import com.dm.DM_Backend.domain.account.oauth2.service.OAuth2Service;
import com.dm.DM_Backend.global.common.response.ApiResponse;
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
@RequestMapping("/rest-api/v1/oauth2")
@Tag(name = "OAuth2", description = "소셜로그인 API")
@RequiredArgsConstructor
public class ApiV1OAuth2Controller {
    private final AuthService authService;
    private final OAuth2Service oAuth2Service;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Value("${frontend.oauth-redirect}")
    private String frontendRedirect;

    // 소셜 로그인 리디렉션 URL
    @GetMapping("/redirect-url/{provider}")
    @Operation(summary = "소셜 로그인 리디렉션 URL")
    public ApiResponse<String> redirectToProvider(@PathVariable("provider") String provider) {
        String authUrl = oAuth2Service.getAuthUrl(provider);
        return ApiResponse.of(authUrl);
    }

    // 소셜 로그인
    @GetMapping("/{provider}")
    @Operation(summary = "소셜 로그인")
    public ResponseEntity<Void> socialLogin(
            @PathVariable("provider") String provider,
            @RequestParam("code") String code) {

        // 소셜 유저 정보 조회
        Map<String, String> socialUser = oAuth2Service.getUserInfo(provider, code);
        String email = socialUser.get("email");
        String name = socialUser.get("name");

        // 로그인 또는 회원가입 처리
        LoginForm socialLoginForm;
        if (memberRepository.existsByEmail(email)) {
            socialLoginForm = LoginForm.builder().email(email).build();
        } else {
            MemberForm memberForm = MemberForm.builder()
                    .email(email)
                    .name(name)
                    .nickname(name)
                    .phone(null)
                    .gender(null)
                    .birthday(null)
                    .password("")
                    .build();
            Member newMember = memberService.signup(memberForm);
            socialLoginForm = LoginForm.builder().email(newMember.getEmail()).build();
        }
        Auth auth = authService.login(socialLoginForm, true);
        String accessToken = auth.getAccessToken();
        String refreshToken = auth.getRefreshToken();

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
