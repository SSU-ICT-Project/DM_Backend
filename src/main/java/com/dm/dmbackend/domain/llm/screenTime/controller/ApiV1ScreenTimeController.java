package com.dm.dmbackend.domain.llm.screenTime.controller;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUser;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeCoachRequest;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeReviewRequest;
import com.dm.dmbackend.domain.llm.screenTime.dto.res.ScreenTimeMessageResponse;
import com.dm.dmbackend.domain.llm.screenTime.service.ScreenTimeService;
import com.dm.dmbackend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest-api/v1/screenTime")
@RequiredArgsConstructor
@Tag(name = "ScreenTime", description = "스크린타임 API")
public class ApiV1ScreenTimeController {
    private final ScreenTimeService screenTimeService;

    // 스크린타임 리뷰 메시지 생성
    @PostMapping("/review")
    @Operation(summary = "스크린타임 리뷰 메시지 생성")
    public ApiResponse<ScreenTimeMessageResponse> getScreenTimeReview(@RequestBody @Valid ScreenTimeReviewRequest screenTimeReviewRequest,
                                                                      @LoginUser LoginUserDto loginUser) {
        return ApiResponse.of(screenTimeService.getScreenTimeReview(screenTimeReviewRequest,loginUser));
    }

    // 스크린타임 코칭 메시지 생성
    @PostMapping("/coach")
    @Operation(summary = "스크린타임 코칭 메시지 생성")
    public ApiResponse<ScreenTimeMessageResponse> getScreenTimeCoach(@RequestBody @Valid ScreenTimeCoachRequest screenTimeCoachRequest,
                                                                     @LoginUser LoginUserDto loginUser) {
        return ApiResponse.of(screenTimeService.getScreenTimeCoach(screenTimeCoachRequest,loginUser));
    }
}
