package com.dm.DM_Backend.domain.notification.controller;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUser;
import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.notification.dto.req.NotificationForm;
import com.dm.DM_Backend.domain.notification.dto.res.NotificationDto;
import com.dm.DM_Backend.domain.notification.entity.NotificationPage;
import com.dm.DM_Backend.domain.notification.service.NotificationService;
import com.dm.DM_Backend.global.common.response.ApiResponse;
import com.dm.DM_Backend.global.common.response.LetzgoPage;
import com.dm.DM_Backend.global.exception.ReturnCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest-api/v1/notification")
@RequiredArgsConstructor
public class ApiV1NotificationController {
    private final NotificationService notificationService;

    // 알림 목록 조회
    @GetMapping
    public ApiResponse<NotificationDto> getNotifications(@ModelAttribute NotificationPage request, @LoginUser LoginUserDto loginUser) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return ApiResponse.of(LetzgoPage.of(notificationService.getNotifications(pageable, loginUser)));
    }

    // 알림 읽음 처리
    @PutMapping
    public ApiResponse<String> markAsRead(@RequestBody @Valid NotificationForm notificationForm,
                                          @LoginUser LoginUserDto loginUser) {
        notificationService.markAsRead(notificationForm, loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }
}
