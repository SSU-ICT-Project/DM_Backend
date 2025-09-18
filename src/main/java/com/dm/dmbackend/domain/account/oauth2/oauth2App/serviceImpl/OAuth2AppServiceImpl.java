package com.dm.dmbackend.domain.account.oauth2.oauth2App.serviceImpl;

import com.dm.dmbackend.domain.account.auth.dto.req.LoginRequest;
import com.dm.dmbackend.domain.account.auth.dto.res.LoginResponse;
import com.dm.dmbackend.domain.account.auth.service.AuthService;
import com.dm.dmbackend.domain.account.member.dto.req.MemberRequest;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.oauth2.oauth2App.config.OAuth2AppProperties;
import com.dm.dmbackend.domain.account.oauth2.oauth2App.dto.req.OAuth2AppLoginRequest;
import com.dm.dmbackend.domain.account.oauth2.oauth2App.dto.res.OAuth2AppTokensResponse;
import com.dm.dmbackend.global.common.enums.OAuth2Provider;
import com.dm.dmbackend.domain.account.oauth2.identity.service.IdentityService;
import com.dm.dmbackend.domain.account.oauth2.oauth2App.service.OAuth2AppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2AppServiceImpl implements OAuth2AppService {
    private final OAuth2AppProperties props;
    private final AuthService authService;
    private final IdentityService identityService;
    private final WebClient webClient;
    private record SocialProfileCandidate(String socialId, MemberRequest candidate) {}

    // 앱 소셜 로그인(회원가입)
    @Override
    public OAuth2AppTokensResponse appLogin(OAuth2Provider provider, OAuth2AppLoginRequest req) {
        // 1) 토큰 검증 & 프로필 → 가입 후보 DTO
        SocialProfileCandidate profile = switch (provider) {
            case GOOGLE -> googleProfile(req.getIdToken());
            case KAKAO  -> kakaoProfile(req.getAccessToken());
            case NAVER  -> naverProfile(req.getAccessToken());
        };
        // 2) (provider, socialId) 기준으로 멤버 보장(없으면 생성 후 연결)
        Member member = identityService.ensureMemberBySocial(provider, profile.socialId(), profile.candidate());

        // 2) 로그인(JWT 발급) — 소셜 플래그로 비번 검증 생략
        LoginResponse login = authService.login(
                LoginRequest.builder().email(member.getEmail()).build(),
                true
        );
        return new OAuth2AppTokensResponse(login.getAccessToken(), login.getRefreshToken());
    }

    // ----------------- Google: ID Token 검증 -----------------
    private SocialProfileCandidate googleProfile(String idToken) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token={idToken}";
        Map<String, Object> body = webClient.get()
                .uri(url, idToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
        if (body == null) throw new IllegalStateException("Google tokeninfo 실패");
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
        String sub   = (String) body.get("sub"); // 소셜 고유 ID
        String email = (String) body.get("email");
        String name  = (String) body.getOrDefault("name", "GoogleUser");
        return new SocialProfileCandidate(
                sub,
                buildMemberForm(name, email, null, null)
        );
    }

    // ----------------- Kakao: /v2/user/me -----------------
    @SuppressWarnings("unchecked")
    private SocialProfileCandidate kakaoProfile(String accessToken) {
        Map<String, Object> body = webClient.get()
                .uri(props.getProviders().get("kakao").getUserInfoUri())
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
        if (body == null) throw new IllegalStateException("Kakao 사용자 조회 실패");
        String id = String.valueOf(body.get("id")); // 소셜 고유 ID

        Map<String, Object> account = (Map<String, Object>) body.get("kakao_account");
        Map<String, Object> profile = account != null ? (Map<String, Object>) account.get("profile") : null;

        String nickname = profile != null
                ? (String) profile.getOrDefault("nickname", "KakaoUser")
                : "KakaoUser";
        String email = account != null ? (String) account.get("email") : null;
        if (email == null || email.isBlank()) {
            email = "kakao_" + id + "@example.com"; // 방어적 플레이스홀더
        }
        return new SocialProfileCandidate(
                id,
                buildMemberForm(nickname, email, null, null)
        );
    }

    // ----------------- Naver: /v1/nid/me -----------------
    @SuppressWarnings("unchecked")
    private SocialProfileCandidate naverProfile(String accessToken) {
        Map<String, Object> outer = webClient.get()
                .uri(props.getProviders().get("naver").getUserInfoUri())
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
        if (outer == null) throw new IllegalStateException("Naver 사용자 조회 실패");

        Map<String, Object> response = (Map<String, Object>) outer.get("response");
        if (response == null) throw new IllegalStateException("Naver response 누락");

        String id        = (String) response.get("id"); // 소셜 고유 ID
        String email     = (String) response.get("email");
        String nickname  = (String) response.getOrDefault("nickname", "NaverUser");
        String genderStr = (String) response.get("gender");
        String birthyear = (String) response.get("birthyear");
        String birthday  = (String) response.get("birthday");

        Member.Gender gender = toGender(genderStr);
        LocalDate birthDate  = parseNaverBirthday(birthyear, birthday);
        return new SocialProfileCandidate(
                id,
                buildMemberForm(nickname, email, gender, birthDate)
        );
    }

    // ----------------- 헬퍼 메서드 -----------------

    // 필요한 필드만 채워 MemberForm 생성 (비밀번호 null)
    private MemberRequest buildMemberForm(String nickname, String email, Member.Gender gender, LocalDate birthday) {
        return MemberRequest.builder()
                .nickname(nickname)
                .job(null)
                .email(email)
                .password(null)                   // 소셜은 비번 없음
                .motivationType(null)
                .gender(gender)
                .birthday(birthday)
                .averagePreparationTime((LocalTime) null)
                .distractionAppList((List<String>) null)
                .build();
    }

    // Naver: birthyear("YYYY") + birthday("MM-DD") → LocalDate
    private LocalDate parseNaverBirthday(String birthyear, String birthdayMMDD) {
        try {
            if (birthyear != null && birthdayMMDD != null && birthdayMMDD.length() == 5) {
                int year  = Integer.parseInt(birthyear);
                int month = Integer.parseInt(birthdayMMDD.substring(0, 2));
                int day   = Integer.parseInt(birthdayMMDD.substring(3, 5));
                return LocalDate.of(year, month, day);
            }
        } catch (Exception e) {
            log.warn("Naver 생일 파싱 실패: birthyear={}, birthday={}, err={}", birthyear, birthdayMMDD, e.toString());
        }
        return null; // 년도 없으면 LocalDate 생성 불가 → 저장 생략
    }

    // "M"/"F" → Member.Gender
    private Member.Gender toGender(String s) {
        if (s == null) return null;
        String v = s.trim().toLowerCase();
        if (v.equals("m")) return Member.Gender.MALE;
        if ( v.equals("f")) return Member.Gender.FEMALE;
        return null;
    }
}
