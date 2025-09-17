package com.dm.dmbackend.domain.account.oauth2.oauth2Web.service;

import com.dm.dmbackend.global.common.enums.OAuth2Provider;

import java.util.Map;

public interface OAuth2WebService {
    // 웹 소셜 로그인 리디렉션 URL 생성
    String getAuthUrl(OAuth2Provider provider);

    // 웹 소셜 로그인
    Map<String, String> getUserInfo(String provider, String code);
}
