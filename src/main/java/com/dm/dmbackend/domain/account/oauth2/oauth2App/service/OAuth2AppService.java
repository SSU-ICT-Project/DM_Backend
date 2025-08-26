package com.dm.dmbackend.domain.account.oauth2.oauth2App.service;

import com.dm.dmbackend.domain.account.member.dto.req.MemberForm;

public interface OAuth2AppService {
    MemberForm toMemberForm(String provider, Object requestBody);
}
