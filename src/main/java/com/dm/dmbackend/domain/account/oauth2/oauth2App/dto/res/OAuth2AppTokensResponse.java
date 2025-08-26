package com.dm.dmbackend.domain.account.oauth2.oauth2App.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OAuth2AppTokensResponse {
    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;
}
