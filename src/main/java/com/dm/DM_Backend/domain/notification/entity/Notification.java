package com.dm.DM_Backend.domain.notification.entity;

import com.dm.DM_Backend.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {
    private Long senderId;
    private String senderNickname;
    private String senderProfileUrl;
    private Long receiverId;
    private Long objectId;
    private String title;
    private String content;

    @Builder.Default
    private Boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TargetObject targetObject;
    public enum TargetObject {
        Comment, Follow, Post
    }
}
