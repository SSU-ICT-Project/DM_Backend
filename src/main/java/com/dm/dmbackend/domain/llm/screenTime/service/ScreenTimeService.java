package com.dm.dmbackend.domain.llm.screenTime.service;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeCureRequest;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeMotivateRequest;

public interface ScreenTimeService {
    // 중독 치료 메시지 생성
    void getScreenTimeCure(ScreenTimeCureRequest screenTimeCureRequest, LoginUserDto loginUser);

    // 동기부여 메시지 생성
    void getScreenTimeMotivate(ScreenTimeMotivateRequest screenTimeMotivateRequest, LoginUserDto loginUser);
}
