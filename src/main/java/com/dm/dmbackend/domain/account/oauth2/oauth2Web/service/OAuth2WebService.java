package com.dm.dmbackend.domain.account.oauth2.oauth2Web.service;

import java.util.Map;

public interface OAuth2WebService {
    // 소셜 로그인 리디렉션 URL 생성
    String getAuthUrl(String provider);

    // 소셜 로그인
    Map<String, String> getUserInfo(String provider, String code);
}
