package com.dm.dmbackend.domain.account.member.dto.res;

import com.dm.dmbackend.domain.account.member.entity.Member;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class DetailMemberDto {
    private Long id;
    private String name;
    private String nickname;
    private String job;
    private String phone;
    private String email;
    private LocalDate birthday;
    private LocalTime averagePreparationTime;
    private List<String> distractionAppList;
    private Boolean useNotification;
    private Member.MotivationType motivationType;
    private Member.Gender gender;
    private String profileImageUrl;
    private Long followMemberCount;
    private Long followedMemberCount;
    private List<SimpleMember> followList;
    private List<SimpleMember> followedList;
    private List<SimpleMember> followReqList;
    private List<SimpleMember> followRecList;
}
