package com.dm.DM_Backend.domain.account.member.dto.req;

import com.dm.DM_Backend.domain.account.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberForm {
    private String name;
    private String nickname;
    private String phone;
    private String email;
    private String password;
    private Member.Gender gender;
    private LocalDate birthday;
}
