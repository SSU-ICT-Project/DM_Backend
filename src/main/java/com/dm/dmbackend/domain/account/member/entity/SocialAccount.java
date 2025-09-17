package com.dm.dmbackend.domain.account.member.entity;

import com.dm.dmbackend.global.common.enums.OAuth2Provider;
import com.dm.dmbackend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "social_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_social_provider_uid", columnNames = {"provider", "provider_user_id"})
        },
        indexes = {
                @Index(name = "idx_social_member_id", columnList = "member_id")
        })
@Setter @Getter
@AllArgsConstructor @NoArgsConstructor
@SuperBuilder
@DynamicInsert
public class SocialAccount extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OAuth2Provider provider;

    @Column(name = "provider_user_id", nullable = false, length = 128)
    private String providerUserId; // google.sub / kakao.id / naver.id

    // 선택 메타(있으면 저장, 없어도 됨)
    private String providerEmail;
    private String displayName;
}
