package com.dm.dmbackend.global.kafka.event.fcm;

import com.dm.dmbackend.domain.fcm.dto.res.FcmMessageResponse;
import com.dm.dmbackend.domain.fcm.service.FcmIdempotencyService;
import com.dm.dmbackend.domain.fcm.service.FcmService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static com.dm.dmbackend.global.constants.KafkaKey.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmEventConsumer {
    private final ObjectMapper objectMapper;
    private final FcmService fcmService;
    private final FcmIdempotencyService fcmIdempotencyService;

    @KafkaListener(topics = FCM_SEND_TOPIC, groupId = FCM_GROUP_ID, containerFactory = KAFKA_LISTENER_CONTAINER)
    public void consumeFcm(ConsumerRecord<String, String> rec) {
        String json = rec.value();
        final FcmMessageResponse msg;
        try {
            msg = objectMapper.readValue(json, FcmMessageResponse.class);
        } catch (Exception e) {
            log.error("Failed to deserialize FCM message: {}", json, e);
            throw new IllegalArgumentException("Invalid FCM message payload", e);
        }
        if (!StringUtils.hasText(msg.getEventId())) {
            throw new IllegalArgumentException("FCM eventId is blank");
        }
        if (!StringUtils.hasText(msg.getTargetToken())) {
            throw new IllegalArgumentException("FCM targetToken is blank");
        }
        // 멱등 선점(이미 처리했으면 스킵)
        boolean first = fcmIdempotencyService.tryAcquire(msg.getEventId());
        if (!first) {
            log.info("Duplicate FCM event skipped. eventId={}, topic={}, offset={}",
                    msg.getEventId(), rec.topic(), rec.offset());
            return;
        }
        try {
            fcmService.sendMessageTo(msg);
            fcmIdempotencyService.markSuccess(msg.getEventId());
        } catch (Exception e) {
            // 재시도 가능하도록 락 해제 후 throw
            fcmIdempotencyService.release(msg.getEventId());
            log.error("Failed to send FCM. eventId={}, topic={}, key={}, offset={}",
                    msg.getEventId(), rec.topic(), rec.key(), rec.offset(), e);
            throw e;
        }
    }
}
