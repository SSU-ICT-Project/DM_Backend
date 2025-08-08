package com.dm.DM_Backend.domain.notification.service;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.notification.dto.req.NotificationForm;
import com.dm.DM_Backend.domain.notification.dto.res.NotificationDto;
import com.dm.DM_Backend.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    // 알림 생성
    void createNotification(Notification notification);

    // 알림 목록 조회
    Page<NotificationDto> getNotifications(Pageable pageable, LoginUserDto loginUser);

    // 알림 읽음 처리
    void markAsRead(NotificationForm notificationForm, LoginUserDto loginUser);

    // Notification을 NotificationDto로 변환
    NotificationDto convertToNotificationDto(Notification notification);
}
