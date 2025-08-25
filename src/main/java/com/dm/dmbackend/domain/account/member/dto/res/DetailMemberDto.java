package com.dm.dmbackend.domain.account.member.dto.res;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.entity.TimeRange;
import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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
    private Map<DayOfWeek, List<TimeRange>> devTimePerDay;
    private List<String> distractionAppList;
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
