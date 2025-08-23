package com.dm.dmbackend.domain.llm.screenTime.service;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeCoachRequest;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeReviewRequest;
import com.dm.dmbackend.domain.llm.screenTime.dto.res.ScreenTimeMessageResponse;

public interface ScreenTimeService {
    // 스크린타임 리뷰 메시지 생성
    ScreenTimeMessageResponse getScreenTimeReview(ScreenTimeReviewRequest screenTimeReviewRequest, LoginUserDto loginUser);

    // 스크린타임 코칭 메시지 생성
    ScreenTimeMessageResponse getScreenTimeCoach(ScreenTimeCoachRequest screenTimeCoachRequest, LoginUserDto loginUser);
}
