package com.dm.DM_Backend.domain.account.oauth2.serviceImpl;

import com.dm.DM_Backend.domain.account.oauth2.dto.res.OAuth2Properties;
import com.dm.DM_Backend.domain.account.oauth2.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {
    private final RestTemplate restTemplate = new RestTemplate();
    private final OAuth2Properties oAuth2Properties;

    // 소셜 로그인 리디렉션 URL 생성
    public String getAuthUrl(String provider) {
        OAuth2Properties.ProviderProperties providerProps = oAuth2Properties.getProviders().get(provider);
        if (providerProps == null) {
            throw new IllegalArgumentException("지원되지 않는 OAuth2 제공자: " + provider);
        }
        String authUrl;
        switch (provider.toLowerCase()) {
            case "google":
                authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                        + "?client_id=" + providerProps.getClientId()
                        + "&redirect_uri=" + providerProps.getRedirectUri()
                        + "&response_type=code"
                        + "&scope=openid%20profile%20email"
                        + "&state=" + generateState();
                break;
            case "kakao":
                authUrl = "https://kauth.kakao.com/oauth/authorize"
                        + "?client_id=" + providerProps.getClientId()
                        + "&redirect_uri=" + providerProps.getRedirectUri()
                        + "&response_type=code"
                        + "&scope=account_email";
                break;
            case "naver":
                authUrl = "https://nid.naver.com/oauth2.0/authorize"
                        + "?client_id=" + providerProps.getClientId()
                        + "&redirect_uri=" + providerProps.getRedirectUri()
                        + "&response_type=code"
                        + "&scope=email";
                break;
            default:
                throw new IllegalArgumentException("지원되지 않는 OAuth2 제공자: " + provider);
        }
        return authUrl;
    }

    private String generateState() {
        return UUID.randomUUID().toString(); // CSRF 방지용 랜덤 값
    }

    // 소셜 로그인
    public Map<String, String> getUserInfo(String provider, String code) {
        OAuth2Properties.ProviderProperties providerProps = oAuth2Properties.getProviders().get(provider);
        if (providerProps == null) {
            throw new IllegalArgumentException("지원되지 않는 OAuth2 제공자: " + provider);
        }
        String accessToken = getAccessToken(provider, providerProps, code);
        return getUserInfoFromProvider(provider, providerProps.getUserInfoUri(), accessToken);
    }

    private String getAccessToken(String provider, OAuth2Properties.ProviderProperties providerProps, String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", providerProps.getClientId());
        params.add("redirect_uri", providerProps.getRedirectUri());
        params.add("grant_type", "authorization_code");

        // 카카오는 client_secret을 포함하지 않음
        if (!"kakao".equalsIgnoreCase(provider) && providerProps.getClientSecret() != null) {
            params.add("client_secret", providerProps.getClientSecret());
        }
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                providerProps.getTokenUri(),
                HttpMethod.POST,
                request,
                Map.class
        );
        if (response.getBody() == null || response.getBody().get("access_token") == null) {
            throw new RuntimeException("OAuth2 액세스 토큰을 가져올 수 없습니다.");
        }
        return (String) response.getBody().get("access_token");
    }

    private Map<String, String> getUserInfoFromProvider(String provider, String userInfoUri, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        // 카카오는 "application/x-www-form-urlencoded;charset=utf-8" 설정 필요
        if ("kakao".equals(provider)) {
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                entity,
                Map.class
        );
        if (response.getBody() == null) {
            throw new RuntimeException("OAuth2 사용자 정보를 가져올 수 없습니다.");
        }

        Map<String, String> userInfo = new HashMap<>();
        if ("google".equals(provider)) {
            String email = (String) response.getBody().get("email");
            String name = (String) response.getBody().get("name");

            userInfo.put("email", email != null ? email : provider + "_unknown@example.com");
            userInfo.put("name", name != null ? name : "Unknown " + provider + "User");
        } else if ("kakao".equals(provider)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) response.getBody().get("kakao_account");
            if (kakaoAccount != null) {
                String email = (String) kakaoAccount.get("email");
                if (email == null) {
                    email = "kakao_" + response.getBody().get("id") + "@example.com"; // 임시 이메일 생성
                }
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                String name = profile != null ? (String) profile.get("nickname") : "Unknown KakaoUser";

                userInfo.put("email", email);
                userInfo.put("name", name);
            } else {
                userInfo.put("email", "kakao_unknown@example.com");
                userInfo.put("name", "Unknown KakaoUser");
            }
        } else if ("naver".equals(provider)) {
            Map<String, String> naverResponse = (Map<String, String>) response.getBody().get("response");
            if (naverResponse != null) {
                userInfo.put("email", naverResponse.getOrDefault("email", "unknown@naver.com"));
                userInfo.put("name", naverResponse.getOrDefault("name", "Unknown NaverUser"));
            } else {
                throw new RuntimeException("네이버 사용자 정보를 가져올 수 없습니다.");
            }
        }
        return userInfo;
    }
}
