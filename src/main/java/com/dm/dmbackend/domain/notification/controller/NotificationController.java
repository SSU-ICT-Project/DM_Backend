package com.dm.dmbackend.domain.notification.controller;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUser;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.notification.dto.req.NotificationRequest;
import com.dm.dmbackend.domain.notification.dto.res.NotificationResponse;
import com.dm.dmbackend.domain.notification.entity.NotificationPage;
import com.dm.dmbackend.domain.notification.service.NotificationService;
import com.dm.dmbackend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest-api/v1/notification")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {
    private final NotificationService notificationService;

    // 알림 목록 조회
    @GetMapping
    @Operation(summary = "알림 목록 조회")
    public ApiResponse<List<NotificationResponse>> getNotifications(@ModelAttribute NotificationPage notificationPage, @LoginUser LoginUserDto loginUser) {
        Pageable pageable = PageRequest.of(notificationPage.getPage(), notificationPage.getSize());
        return ApiResponse.success(notificationService.getNotifications(pageable, loginUser));
    }

    // 알림 읽음 처리
    @PutMapping
    @Operation(summary = "알림 읽음 처리")
    public ApiResponse<Void> markAsRead(@RequestBody @Valid NotificationRequest notificationRequest,
                                          @LoginUser LoginUserDto loginUser) {
        notificationService.markAsRead(notificationRequest, loginUser);
        return ApiResponse.success();
    }
}
