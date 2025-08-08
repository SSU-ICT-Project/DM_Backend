package com.dm.DM_Backend.domain.account.member.entity;

import com.dm.DM_Backend.global.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class MemberFollow extends BaseEntity {
    // 팔로우 하는 멤버
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follow_id", nullable = false)
    private Member follow;

    // 팔로우 당하는 멤버
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id", nullable = false)
    private Member followed;
}
