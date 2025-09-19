package com.dm.dmbackend.domain.account.oauth2.identity.service;

import com.dm.dmbackend.domain.account.member.dto.req.MemberRequest;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.global.common.enums.OAuth2Provider;

public interface IdentityService {
    // (provider, socialId) 기준으로 멤버 보장
    Member ensureMemberBySocial(OAuth2Provider provider, String socialId, MemberRequest candidate);
}
