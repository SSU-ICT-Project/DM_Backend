package com.dm.dmbackend.domain.notification.controller;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUser;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.notification.dto.req.NotificationForm;
import com.dm.dmbackend.domain.notification.dto.res.NotificationDto;
import com.dm.dmbackend.domain.notification.entity.NotificationPage;
import com.dm.dmbackend.domain.notification.service.NotificationService;
import com.dm.dmbackend.global.common.response.ApiResponse;
import com.dm.dmbackend.global.common.response.DMPage;
import com.dm.dmbackend.global.exception.ReturnCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest-api/v1/notification")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class ApiV1NotificationController {
    private final NotificationService notificationService;

    // 알림 목록 조회
    @GetMapping
    @Operation(summary = "알림 목록 조회")
    public ApiResponse<NotificationDto> getNotifications(@ModelAttribute NotificationPage request, @LoginUser LoginUserDto loginUser) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return ApiResponse.of(DMPage.of(notificationService.getNotifications(pageable, loginUser)));
    }

    // 알림 읽음 처리
    @PutMapping
    @Operation(summary = "알림 읽음 처리")
    public ApiResponse<String> markAsRead(@RequestBody @Valid NotificationForm notificationForm,
                                          @LoginUser LoginUserDto loginUser) {
        notificationService.markAsRead(notificationForm, loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }
}
