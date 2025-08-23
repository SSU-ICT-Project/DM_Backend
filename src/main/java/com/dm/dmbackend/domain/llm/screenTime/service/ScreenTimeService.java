package com.dm.dmbackend.domain.llm.screenTime.service;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeReviewRequest;
import com.dm.dmbackend.domain.llm.screenTime.dto.res.ScreenTimeReviewResponse;

public interface ScreenTimeService {
    // 스크린타임리뷰 생성
    ScreenTimeReviewResponse getScreenTimeReview(ScreenTimeReviewRequest screenTimeReviewRequest, LoginUserDto loginUser);
}
