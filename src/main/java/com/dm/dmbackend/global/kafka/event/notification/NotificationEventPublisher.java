package com.dm.dmbackend.global.kafka.event.notification;

import com.dm.dmbackend.domain.notification.dto.NotificationPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishNotification(NotificationPayload notificationPayload, String topic) {
        try {
            String json = objectMapper.writeValueAsString(notificationPayload);
            kafkaTemplate.send(topic, String.valueOf(notificationPayload.receiverId()), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize notification payload", e);
        }
    }
}
