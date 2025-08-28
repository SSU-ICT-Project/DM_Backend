package com.dm.DM_Backend.domain.account.member.dto.req;

import com.dm.DM_Backend.domain.account.member.entity.Member;
import jakarta.persistence.Column;
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
    private String nickname;
    private String job;
    private String email;
    private String password;
    private Member.MotivationType motivationType;
    private Member.Gender gender;
    private LocalDate birthday;

    private String placeAddress;
    private String latitude;
    private String longitude;

}
