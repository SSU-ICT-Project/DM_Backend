package com.dm.dmbackend.domain.account.auth.loginUser;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.entity.MemberFollow;
import com.dm.dmbackend.domain.account.member.entity.MemberFollowReq;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class LoginUserDto {
    private Long id;
    private String name;
    private String nickname;
    private String job;
    private String phone;
    private String email;
    private String password;
    private Member.MotivationType motivationType;
    private Member.Gender gender;
    private LocalDate birthday;
    private LocalTime averagePreparationTime;
    private List<String> distractionAppList;
    private Boolean useNotification;
    private Member.State state;
    private Member.MemberRole role;
    private String profileImageUrl;
    private List<MemberFollow> followList;
    private List<MemberFollow> followedList;
    private List<MemberFollowReq> followReqList;
    private List<MemberFollowReq> followRecList;
    private LocalDateTime createdAt;

    // Member 객체를 LoginUserDto로 변환하는 정적 팩토리 메서드
    public static LoginUserDto ConvertToLoginUserDto(Member member) {
        return LoginUserDto.builder()
                .id(member.getId())
                .name(member.getName())
                .nickname(member.getNickname())
                .job(member.getJob())
                .phone(member.getPhone())
                .email(member.getEmail())
                .password(member.getPassword())
                .birthday(member.getBirthday())
                .averagePreparationTime(member.getAveragePreparationTime())
                .distractionAppList(member.getDistractionAppList())
                .useNotification(member.getUseNotification())
                .motivationType(member.getMotivationType())
                .gender(member.getGender())
                .state(member.getState())
                .role(member.getRole())
                .profileImageUrl(member.getProfileImageUrl())
                .followList(member.getFollowList())
                .followedList(member.getFollowedList())
                .followReqList(member.getFollowReqList())
                .followRecList(member.getFollowRecList())
                .createdAt(member.getCreatedAt())
                .build();
    }

    // LoginUserDto를 Member 엔티티로 변환하는 메서드
    public Member ConvertToMember() {
        return Member.builder()
                .id(this.id)
                .name(this.name)
                .nickname(this.nickname)
                .job(this.job)
                .phone(this.phone)
                .email(this.email)
                .password(this.password)
                .birthday(this.birthday)
                .averagePreparationTime(this.averagePreparationTime)
                .distractionAppList(this.distractionAppList)
                .useNotification(this.useNotification)
                .motivationType(this.motivationType)
                .gender(this.gender)
                .state(this.state)
                .role(this.role)
                .profileImageUrl(this.profileImageUrl)
                .followList(this.followList)
                .followedList(this.followedList)
                .followReqList(this.followReqList)
                .followRecList(this.followRecList)
                .createdAt(this.createdAt)
                .build();
    }
}
