package com.dm.dmbackend.domain.account.member.dto.req;

import com.dm.dmbackend.domain.account.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminForm {
    private String nickname;
    private String job;
    private String email;
    private String password;
    private Member.MotivationType motivationType;
    private Member.Gender gender;
    private Member.MemberRole memberRole;
    private LocalDate birthday;
}
