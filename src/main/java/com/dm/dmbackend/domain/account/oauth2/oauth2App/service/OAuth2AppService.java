package com.dm.dmbackend.domain.account.oauth2.oauth2App.service;

import com.dm.dmbackend.domain.account.oauth2.oauth2App.dto.req.OAuth2AppLoginRequest;
import com.dm.dmbackend.domain.account.oauth2.oauth2App.dto.res.OAuth2AppTokensResponse;
import com.dm.dmbackend.domain.account.oauth2.provider.OAuth2Provider;

public interface OAuth2AppService {
    // 앱 소셜 로그인(회원가입)
    OAuth2AppTokensResponse appLogin(OAuth2Provider provider, OAuth2AppLoginRequest req);
}
