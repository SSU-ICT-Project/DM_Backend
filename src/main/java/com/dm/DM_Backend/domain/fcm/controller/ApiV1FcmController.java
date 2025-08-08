package com.dm.DM_Backend.domain.fcm.controller;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUser;
import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.fcm.dto.req.FcmToken;
import com.dm.DM_Backend.domain.fcm.service.FcmTokenService;
import com.dm.DM_Backend.global.common.response.ApiResponse;
import com.dm.DM_Backend.global.exception.ReturnCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest-api/v1/fcm")
@RequiredArgsConstructor
public class ApiV1FcmController {
    private final FcmTokenService fcmTokenService;

    // FCM Token 저장
    @PostMapping
    public ApiResponse<String> saveFcmToken(@RequestBody @Valid FcmToken fcmToken, @LoginUser LoginUserDto loginUser) {
        fcmTokenService.saveFcmToken(loginUser.getId(), fcmToken.getFcmToken());
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // FCM Token 삭제
    @DeleteMapping
    public ApiResponse<String> deleteFcmToken(@LoginUser LoginUserDto loginUser) {
        fcmTokenService.deleteFcmToken(loginUser.getId());
        return ApiResponse.of(ReturnCode.SUCCESS);
    }
}
