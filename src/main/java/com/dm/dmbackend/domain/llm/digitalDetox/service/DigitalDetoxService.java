package com.dm.dmbackend.domain.llm.digitalDetox.service;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.llm.digitalDetox.dto.req.DigitalDetoxCureRequest;

public interface DigitalDetoxService {
    // 중독 치료 메시지 생성
    void getDigitalDetoxCure(DigitalDetoxCureRequest digitalDetoxCureRequest, LoginUserDto loginUser);

    // 동기부여 메시지 생성
    void getDigitalDetoxMotivate(LoginUserDto loginUser);
}
