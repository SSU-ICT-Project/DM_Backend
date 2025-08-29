package com.dm.dmbackend.global.redis;

import com.dm.dmbackend.domain.notification.entity.Notification;
import com.dm.dmbackend.domain.schedule.scheduleMessage.entity.ScheduleMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleMessageWorker {
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String REDIS_KEY = "schedule:messages";

    // 1분마다 실행 (window = 1분)
    @Scheduled(cron = "0 * * * * *")
    public void processDueMessages() {
        long now = Instant.now().toEpochMilli();
        long window = 60 * 1000; // 1분
        long maxScore = now + window;

        // Redis ZSET에서 지금 보낼 메시지 조회
        Set<Object> dueMessages = redisTemplate.opsForZSet().rangeByScore(REDIS_KEY, 0, maxScore);
        if (dueMessages == null || dueMessages.isEmpty()) {
            return;
        }
        for (Object msgObj : dueMessages) {
            try {
                String json = (String) msgObj;
                ScheduleMessage scheduleMessage = objectMapper.readValue(json, ScheduleMessage.class);
                // Kafka로 전송
                Notification notification = Notification.builder()
                        .senderId(scheduleMessage.getMember().getId())
                        .senderNickname(scheduleMessage.getMember().getNickname())
                        .senderProfileUrl(scheduleMessage.getMember().getProfileImageUrl())
                        .receiverId(scheduleMessage.getMember().getId())
                        .objectId(scheduleMessage.getId())
                        .content(scheduleMessage.getMessage())
                        .targetObject(Notification.TargetObject.SCHEDULE)
                        .build();
                String kafkaMessage = objectMapper.writeValueAsString(notification);
                kafkaTemplate.send("schedule-topic", kafkaMessage);

                // 발송 완료된 메시지는 Redis에서 제거
                redisTemplate.opsForZSet().remove(REDIS_KEY, json);
                log.info("Sent scheduled message to Kafka. scheduleId={}", scheduleMessage.getId());
            } catch (Exception e) {
                log.error("Failed to process scheduled message: {}", e.getMessage());
            }
        }
    }
}
