package com.dm.dmbackend.domain.fcm.service;

public interface FcmIdempotencyService {
    // 처음 처리하는 eventId면 true, 이미 처리(또는 처리중)이면 false
    boolean tryAcquire(String eventId);

    // 처리 성공 표시: TTL 연장하거나 상태 바꿀 때 사용
    void markSuccess(String eventId);

    // 처리 실패 시: 재시도를 위해 락 해제
    void release(String eventId);
}
