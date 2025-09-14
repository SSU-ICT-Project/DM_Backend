package com.dm.dmbackend.domain.account.member.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberResponse {
    private Long id;
    private String name;
    private String nickname;
    private String profileImageUrl;
    private Long followMemberCount;
    private Long followedMemberCount;
}
