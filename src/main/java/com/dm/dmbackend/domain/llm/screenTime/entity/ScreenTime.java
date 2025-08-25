package com.dm.dmbackend.domain.llm.screenTime.entity;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@DynamicInsert
public class ScreenTime extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 100)
    private String screenTimeData;

    @Column(length = 20)
    private String accessAppData;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private MessageType messageType;
    public enum MessageType {
        CURE,
        MOTIVATE
    }
}
