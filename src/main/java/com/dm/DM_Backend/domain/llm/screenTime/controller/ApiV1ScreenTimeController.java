package com.dm.DM_Backend.domain.llm.screenTime.controller;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUser;
import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.llm.screenTime.dto.req.ScreenTimeReviewRequest;
import com.dm.DM_Backend.domain.llm.screenTime.dto.res.ScreenTimeReviewResponse;
import com.dm.DM_Backend.domain.llm.screenTime.service.ScreenTimeService;
import com.dm.DM_Backend.global.common.response.ApiResponse;
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

    // 스크린타임리뷰 메시지 생성
    @PostMapping("/reviewMessage")
    @Operation(summary = "스크린타임리뷰 메시지 생성")
    public ApiResponse<ScreenTimeReviewResponse> getScreenTimeReview(@RequestBody @Valid ScreenTimeReviewRequest screenTimeReviewRequest,
                                                                     @LoginUser LoginUserDto loginUser) {
        return ApiResponse.of(screenTimeService.getScreenTimeReview(screenTimeReviewRequest,loginUser));
    }
}
