package com.dm.DM_Backend.domain.account.member.entity;

import com.dm.DM_Backend.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@DynamicInsert
public class Member extends BaseEntity {
    @Column(length = 20)
    private String name;

    @Column(length = 20)
    private String nickname;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String email;

    @Column(length = 1000)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 6)
    private Gender gender;  // 성별
    public enum Gender {
        MALE, FEMALE;
    }

    @Enumerated(EnumType.STRING)
    @Column(length = 6, nullable = false)
    @Builder.Default
    private State state = State.NORMAL;  // 회원 상태
    public enum State {
        NORMAL, BANNED;
    }

    @Enumerated(EnumType.STRING)
    @Column(length = 9, nullable = false)
    @Builder.Default
    private MemberRole role = MemberRole.ROLE_USER;  // 권한 (관리자, 사용자)
    public enum MemberRole {
        ROLE_USER, ROLE_ADMIN;
    }

    @Column(length = 300)
    private String profileImageUrl = "";  // 프로필 사진 경로

    @OneToMany(mappedBy = "follow", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberFollow> followList = new ArrayList<>();

    @OneToMany(mappedBy = "followed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberFollow> followedList = new ArrayList<>();

    @OneToMany(mappedBy = "followReq", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberFollowReq> followReqList = new ArrayList<>();

    @OneToMany(mappedBy = "followRec", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberFollowReq> followRecList = new ArrayList<>();

    private LocalDate birthday;
}
