package com.dm.DM_Backend.domain.account.member.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberDto {
    private Long id;
    private String name;
    private String nickname;
    private String profileImageUrl;
    private Long followMemberCount;
    private Long followedMemberCount;
}
