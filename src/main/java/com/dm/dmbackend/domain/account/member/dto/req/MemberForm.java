package com.dm.dmbackend.domain.account.member.dto.req;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.global.common.vo.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberForm {
    private String nickname;
    private String job;
    private String email;
    private String password;
    private LocalDate birthday;
    private LocalTime averagePreparationTime;
    private List<String> distractionAppList;
    private Location location;
    private Boolean useNotification;
    private Member.MotivationType motivationType;
    private Member.Gender gender;
}
