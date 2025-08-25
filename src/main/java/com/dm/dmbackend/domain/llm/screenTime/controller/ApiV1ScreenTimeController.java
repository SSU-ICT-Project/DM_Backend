package com.dm.dmbackend.domain.llm.screenTime.controller;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUser;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeCureRequest;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeMotivateRequest;
import com.dm.dmbackend.domain.llm.screenTime.service.ScreenTimeService;
import com.dm.dmbackend.global.common.response.ApiResponse;
import com.dm.dmbackend.global.exception.ReturnCode;
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

    // 중독 치료 메시지 생성
    @PostMapping("/cure")
    @Operation(summary = "중독 치료 메시지 생성")
    public ApiResponse<String> getScreenTimeCure(@RequestBody @Valid ScreenTimeCureRequest screenTimeCureRequest,
                                                                      @LoginUser LoginUserDto loginUser) {
        screenTimeService.getScreenTimeCure(screenTimeCureRequest,loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 동기부여 메시지 생성
    @PostMapping("/motivate")
    @Operation(summary = "동기부여 메시지 생성")
    public ApiResponse<String> getScreenTimeMotivate(@RequestBody @Valid ScreenTimeMotivateRequest screenTimeMotivateRequest,
                                                                     @LoginUser LoginUserDto loginUser) {
        screenTimeService.getScreenTimeMotivate(screenTimeMotivateRequest, loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }
}
