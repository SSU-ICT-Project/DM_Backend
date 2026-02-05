package com.dm.dmbackend.domain.notification.serviceImpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.entity.MemberPage;
import com.dm.dmbackend.domain.notification.dto.req.NotificationRequest;
import com.dm.dmbackend.domain.notification.dto.res.NotificationResponse;
import com.dm.dmbackend.domain.notification.entity.Notification;
import com.dm.dmbackend.domain.notification.repository.NotificationRepository;
import com.dm.dmbackend.domain.notification.service.NotificationService;
import com.dm.dmbackend.global.common.response.PageResponse;
import com.dm.dmbackend.global.exception.ReturnCode;
import com.dm.dmbackend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    // 알림 생성
    @Override
    @Transactional
    public void createNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    // 알림 목록 조회
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(Pageable pageable, LoginUserDto loginUser) {
        checkPageSize(pageable.getPageSize());
        Page<Notification> notificationPage = notificationRepository.findByReceiverIdOrderByCreatedAtDesc(loginUser.getId(), pageable);
        return PageResponse.of(notificationPage.map(this::convertToNotificationDto));
    }

    // 알림 읽음 처리
    @Override
    @Transactional
    public void markAsRead(NotificationRequest notificationRequest, LoginUserDto loginUser) {
        // 본인 알림인지 확인
        List<Long> ids = notificationRequest.getNotificationIdList();
        List<Notification> notifications = notificationRepository.findAllByIdInAndReceiverId(ids, loginUser.getId());
        if (notifications.size() != ids.size()) {
            throw new ServiceException(ReturnCode.NOTIFICATION_NOT_FOUND);
        }
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    // ----------------- 헬퍼 메서드 -----------------

    // 요청 페이지 수 제한
    private void checkPageSize(int pageSize) {
        int maxPageSize = MemberPage.getMaxPageSize();
        if (pageSize > maxPageSize) {
            throw new ServiceException(ReturnCode.PAGE_REQUEST_FAIL);
        }
    }

    // Notification을 NotificationDto로 변환
    @Override
    public NotificationResponse convertToNotificationDto(Notification notification){
        return NotificationResponse.builder()
                .id(notification.getId())
                .senderId(notification.getSenderId())
                .senderNickname(notification.getSenderNickname())
                .senderProfileUrl(notification.getSenderProfileUrl())
                .receiverId(notification.getReceiverId())
                .objectId(notification.getObjectId())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .targetObject(notification.getTargetObject())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
