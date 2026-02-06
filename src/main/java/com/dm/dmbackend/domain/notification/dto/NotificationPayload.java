package com.dm.dmbackend.domain.notification.dto;

import com.dm.dmbackend.domain.notification.entity.Notification;

public record NotificationPayload(
        Long senderId,
        String senderNickname,
        String senderProfileUrl,
        Long receiverId,
        Long objectId,
        String content,
        Notification.TargetObject targetObject
) {}
