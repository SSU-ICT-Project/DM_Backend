package com.dm.DM_Backend.domain.notification.dto.res;

import com.dm.DM_Backend.domain.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private Long senderId;
    private String senderNickname;
    private String senderProfileUrl;
    private Long receiverId;
    private Long objectId;
    private String content;
    private Boolean isRead;
    private Notification.TargetObject targetObject;
    private LocalDateTime createdAt;
}
