package com.dm.dmbackend.domain.account.oauth2.oauth2App.serviceImpl;

import com.dm.dmbackend.domain.account.member.dto.req.MemberForm;
import com.dm.dmbackend.domain.account.oauth2.oauth2App.config.OAuth2Properties;
import com.dm.dmbackend.domain.account.oauth2.oauth2App.service.OAuth2AppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2AppServiceImpl implements OAuth2AppService {
    private final OAuth2Properties props;
    private final RestTemplate rest = new RestTemplate();

    // provider 토큰을 검증하고, 가입 후보 MemberForm 으로 변환
    public MemberForm toMemberForm(String provider, Object requestBody) {
        @SuppressWarnings("unchecked")
        Map<String, Object> req = (Map<String, Object>) requestBody;

        switch (provider.toLowerCase()) {
            case "google":
                String idToken = (String) req.get("idToken");
                Assert.hasText(idToken, "idToken is required for Google");
                return fromGoogleIdToken(idToken);
            case "kakao":
                String kakaoAccess = (String) req.get("accessToken");
                Assert.hasText(kakaoAccess, "accessToken is required for Kakao");
                return fromKakaoAccessToken(kakaoAccess);
            case "naver":
                String naverAccess = (String) req.get("accessToken");
                Assert.hasText(naverAccess, "accessToken is required for Naver");
                return fromNaverAccessToken(naverAccess);
            default:
                throw new IllegalArgumentException("지원되지 않는 provider: " + provider);
        }
    }

    // -------- Google: ID Token 검증 (tokeninfo 사용; 운영은 JWKS 검증 권장) --------
    private MemberForm fromGoogleIdToken(String idToken) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        ResponseEntity<Map> res = rest.getForEntity(url, Map.class);
        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new IllegalStateException("Google tokeninfo 실패");
        }
        Map<String, Object> body = res.getBody();

        // aud 검증
        String aud = (String) body.get("aud");
        String expectedAud = props.getProviders().get("google").getClientId();
        if (expectedAud == null || !expectedAud.equals(aud)) {
            throw new IllegalStateException("Google ID Token aud 불일치");
        }

        // 만료 검증
        String expStr = (String) body.get("exp");
        if (expStr != null) {
            long exp = Long.parseLong(expStr);
            if (Instant.ofEpochSecond(exp).isBefore(Instant.now())) {
                throw new IllegalStateException("Google ID Token 만료");
            }
        }

        String email   = (String) body.get("email");
        String name    = (String) body.getOrDefault("name", "GoogleUser");

        return buildMemberForm(name, email);
    }

    // -------- Kakao: /v2/user/me --------
    @SuppressWarnings("unchecked")
    private MemberForm fromKakaoAccessToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> res = rest.exchange(
                props.getProviders().get("kakao").getUserInfoUri(),
                HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new IllegalStateException("Kakao 사용자 조회 실패");
        }
        Map<String, Object> body = res.getBody();
        String id = String.valueOf(body.get("id"));

        Map<String, Object> account = (Map<String, Object>) body.get("kakao_account");
        String email = account != null ? (String) account.get("email") : null;
        Map<String, Object> profile = account != null ? (Map<String, Object>) account.get("profile") : null;
        String name = profile != null ? (String) profile.getOrDefault("nickname", "KakaoUser") : "KakaoUser";

        if (email == null || email.isBlank()) {
            // 이메일 미제공 대비 (※ SocialAccount 없이 이메일로만 매핑하므로 placeholder 생성)
            email = "kakao_" + id + "@example.com";
        }
        return buildMemberForm(name, email);
    }

    // -------- Naver: /v1/nid/me --------
    @SuppressWarnings("unchecked")
    private MemberForm fromNaverAccessToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> res = rest.exchange(
                props.getProviders().get("naver").getUserInfoUri(),
                HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new IllegalStateException("Naver 사용자 조회 실패");
        }
        Map<String, Object> outer = res.getBody();
        Map<String, Object> response = (Map<String, Object>) outer.get("response");
        if (response == null) throw new IllegalStateException("Naver response 누락");

        String id = (String) response.get("id");
        String email = (String) response.get("email");
        String name  = (String) response.getOrDefault("name", "NaverUser");

        if (email == null || email.isBlank()) {
            email = "naver_" + id + "@example.com";
        }
        return buildMemberForm(name, email);
    }

    // 필요한 필드만 채워 MemberForm 생성 (비밀번호 null)
    private MemberForm buildMemberForm(String nickname, String email) {
        return MemberForm.builder()
                .nickname(nickname)
                .job(null)
                .email(email)
                .password(null)                   // 소셜은 비번 없음
                .motivationType(null)
                .gender(null)
                .birthday((LocalDate) null)
                .averagePreparationTime((LocalTime) null)
                .devTimePerDay(null)
                .distractionAppList((List<String>) null)
                .build();
    }
}
