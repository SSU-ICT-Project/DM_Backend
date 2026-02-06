package com.dm.dmbackend.domain.notification.service;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.notification.dto.req.NotificationRequest;
import com.dm.dmbackend.domain.notification.dto.res.NotificationResponse;
import com.dm.dmbackend.domain.notification.entity.Notification;
import com.dm.dmbackend.global.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    // Kafka 수신 메시지 저장 & FCM 전송
    void createAndNotifyFromMessage(String message, String title);

    // 알림 목록 조회
    PageResponse<NotificationResponse> getNotifications(Pageable pageable, LoginUserDto loginUser);

    // 알림 읽음 처리
    void markAsRead(NotificationRequest notificationRequest, LoginUserDto loginUser);

    // Notification을 NotificationDto로 변환
    NotificationResponse convertToNotificationDto(Notification notification);
}
