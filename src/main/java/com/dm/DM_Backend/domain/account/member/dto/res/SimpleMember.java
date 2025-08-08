package com.dm.DM_Backend.domain.account.member.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimpleMember {
    private Long userId;
    private String userName;
    private String userNickname;
    private String profileImageUrl;
}
