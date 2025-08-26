package com.dm.dmbackend.domain.account.oauth2.oauth2App.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {
    private Map<String, ProviderProperties> providers;

    @Data
    public static class ProviderProperties {
        private String clientId;     // Google: 서버 검증 대상 aud
        private String userInfoUri;  // Kakao/Naver: /me 엔드포인트
    }
}
