package com.dm.DM_Backend.domain.account.oauth2.dto.res;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.oauth2")
@Getter
@Setter
public class OAuth2Properties {
    private Map<String, ProviderProperties> providers = new HashMap<>();

    @Getter
    @Setter
    public static class ProviderProperties {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUri;
        private String userInfoUri;
    }
}
