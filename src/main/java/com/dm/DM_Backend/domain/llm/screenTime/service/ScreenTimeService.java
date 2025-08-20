package com.dm.DM_Backend.domain.llm.screenTime.service;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.llm.screenTime.dto.req.ScreenTimeReviewRequest;
import com.dm.DM_Backend.domain.llm.screenTime.dto.res.ScreenTimeReviewResponse;

public interface ScreenTimeService {
    // 스크린타임리뷰 생성
    ScreenTimeReviewResponse getScreenTimeReview(ScreenTimeReviewRequest request, LoginUserDto loginUser);
}
