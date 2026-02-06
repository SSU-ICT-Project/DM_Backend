package com.dm.dmbackend.domain.notification.mapper;

import com.dm.dmbackend.domain.notification.dto.NotificationPayload;
import com.dm.dmbackend.domain.notification.dto.res.NotificationResponse;
import com.dm.dmbackend.domain.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {
    // ---------- Notification -> NotificationResponse ----------
    @Mapping(target = "id", source = "id")
    @Mapping(target = "senderId", source = "senderId")
    @Mapping(target = "senderNickname", source = "senderNickname")
    @Mapping(target = "senderProfileUrl", source = "senderProfileUrl")
    @Mapping(target = "receiverId", source = "receiverId")
    @Mapping(target = "objectId", source = "objectId")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "isRead", source = "isRead")
    @Mapping(target = "targetObject", source = "targetObject")
    @Mapping(target = "createdAt", source = "createdAt")
    NotificationResponse toNotificationResponse(Notification notification);

    // ---------- NotificationPayload -> Notification ----------
    @Mapping(target = "id", ignore = true)          // BaseEntity PK
    @Mapping(target = "createdAt", ignore = true)   // BaseEntity
    @Mapping(target = "modifiedAt", ignore = true)   // BaseEntity (필드명 다르면 제거)
    @Mapping(target = "isRead", ignore = true)      // 기본값(false) 유지
    @Mapping(target = "title", source = "title")    // 서비스에서 받은 title 주입

    @Mapping(target = "senderId", source = "payload.senderId")
    @Mapping(target = "senderNickname", source = "payload.senderNickname")
    @Mapping(target = "senderProfileUrl", source = "payload.senderProfileUrl")
    @Mapping(target = "receiverId", source = "payload.receiverId")
    @Mapping(target = "objectId", source = "payload.objectId")
    @Mapping(target = "content", source = "payload.content")
    @Mapping(target = "targetObject", source = "payload.targetObject")
    Notification toNotification(NotificationPayload payload, String title);
}
