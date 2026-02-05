package com.dm.dmbackend.domain.account.auth.service;

import java.time.Duration;

public interface AccessTokenDenyListService {
    void deny(String jti, Duration ttl); // 남은 유효시간만큼 차단
    boolean isDenied(String jti);
}
