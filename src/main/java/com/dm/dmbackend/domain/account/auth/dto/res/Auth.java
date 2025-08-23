package com.dm.dmbackend.domain.account.auth.dto.res;

import lombok.Data;

@Data
public class Auth {
    private String accessToken;
    private String refreshToken;

    public Auth(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
