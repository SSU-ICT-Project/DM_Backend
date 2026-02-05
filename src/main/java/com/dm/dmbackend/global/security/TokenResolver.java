package com.dm.dmbackend.global.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TokenResolver {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public String resolveAccess(HttpServletRequest req) {
        // 1) 쿠키 우선
        String byCookie = cookie(req, "access_token");
        if (StringUtils.hasText(byCookie)) return byCookie;

        // 2) Authorization: Bearer
        String bearerToken = req.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    public String resolveRefresh(HttpServletRequest req) {
        // 1) 쿠키 우선
        String byCookie = cookie(req, "refresh_token");
        if (StringUtils.hasText(byCookie)) return byCookie;
        return null;
    }

    private String cookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
