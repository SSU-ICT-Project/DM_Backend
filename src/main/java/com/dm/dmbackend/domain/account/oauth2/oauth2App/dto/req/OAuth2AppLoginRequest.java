package com.dm.dmbackend.domain.account.oauth2.oauth2App.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2AppLoginRequest {
    // Google OIDC
    private String idToken;        // Google: SDK에서 받은 ID Token

    // Kakao / Naver
    private String accessToken;    // Kakao/Naver: SDK에서 받은 Access Token
}
