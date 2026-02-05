package com.dm.dmbackend.domain.llm.digitalDetox.controller;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUser;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.llm.digitalDetox.dto.req.DigitalDetoxCureRequest;
import com.dm.dmbackend.domain.llm.digitalDetox.service.DigitalDetoxService;
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
@RequestMapping("/rest-api/v1/digitalDetox")
@RequiredArgsConstructor
@Tag(name = "DigitalDetox", description = "디지털 디톡스 API")
public class DigitalDetoxController {
    private final DigitalDetoxService digitalDetoxService;

    // 중독 치료 메시지 생성
    @PostMapping("/cure")
    @Operation(summary = "중독 치료 메시지 생성")
    public ApiResponse<Void> getDigitalDetoxCure(@RequestBody @Valid DigitalDetoxCureRequest digitalDetoxCureRequest,
                                                 @LoginUser LoginUserDto loginUser) {
        digitalDetoxService.getDigitalDetoxCure(digitalDetoxCureRequest, loginUser);
        return ApiResponse.success();
    }

    // 동기부여 메시지 생성
    @PostMapping("/motivate")
    @Operation(summary = "동기부여 메시지 생성")
    public ApiResponse<Void> getDigitalDetoxMotivate(@LoginUser LoginUserDto loginUser) {
        digitalDetoxService.getDigitalDetoxMotivate(loginUser);
        return ApiResponse.success();
    }
}
