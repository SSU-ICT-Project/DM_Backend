package com.dm.dmbackend.domain.account.member.dto.req;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.entity.TimeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminForm {
    private String nickname;
    private String job;
    private String email;
    private String password;
    private LocalDate birthday;
    private LocalTime averagePreparationTime;
    private Map<DayOfWeek, List<TimeRange>> devTimePerDay;
    private List<String> distractionAppList;
    private Member.MotivationType motivationType;
    private Member.Gender gender;
    private Member.MemberRole memberRole;
}
