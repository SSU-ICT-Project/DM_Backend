package com.dm.DM_Backend.domain.notification.eventListener;

import com.dm.DM_Backend.domain.fcm.dto.FcmMessage;
import com.dm.DM_Backend.domain.fcm.service.FcmService;
import com.dm.DM_Backend.domain.fcm.service.FcmTokenService;
import com.dm.DM_Backend.domain.notification.dto.res.NotificationDto;
import com.dm.DM_Backend.domain.notification.entity.Notification;
import com.dm.DM_Backend.domain.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationEventListener {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final FcmService fcmService;
    private final FcmTokenService fcmTokenService;
    private final Map<String, NotificationMeta> notificationMetaMap = Map.of(
            "follow-topic", new NotificationMeta("팔로우 알림", "follow-topic-dlt")
    );

    @KafkaListener(topics = {"comment-topic", "follow-topic", "post-topic"}, groupId = "1")
    public void consume(ConsumerRecord<String, String> record) {
        String topic = record.topic();
        String message = record.value();
        NotificationMeta meta = notificationMetaMap.get(topic);
        if (meta == null) {
            log.error("Unknown topic received: {}", topic);
            return;
        }
        handleMessage(message, meta.title(), meta.dltTopic());
    }

    private void handleMessage(String message, String title, String dltTopic) {
        int maxRetries = 3;
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                Notification notification = objectMapper.readValue(message, Notification.class);
                notificationService.createNotification(notification);
                String fcmToken = fcmTokenService.getFcmToken(notification.getReceiverId());
                if (fcmToken != null) {
                    NotificationDto notificationDto = notificationService.convertToNotificationDto(notification);
                    String bodyJson = objectMapper.writeValueAsString(notificationDto); // JSON 직렬화

                    FcmMessage fcmMessage = FcmMessage.builder()
                            .targetToken(fcmToken)
                            .title(title)
                            .body(bodyJson) // JSON 문자열로 설정
                            .build();
                    fcmService.sendMessageTo(fcmMessage);
                } else {
                    log.warn("FCM token not found for receiverId: {}", notification.getReceiverId());
                }
                return;
            } catch (Exception e) {
                attempt++;
                log.error("Retry attempt {} failed: {}", attempt, e.getMessage());
            }
        }
        kafkaTemplate.send(dltTopic, message);
    }
    private record NotificationMeta(String title, String dltTopic) {}
}
