package com.dm.dmbackend.global.redis;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.repository.MemberRepository;
import com.dm.dmbackend.domain.notification.entity.Notification;
import com.dm.dmbackend.domain.schedule.schedule.entity.Schedule;
import com.dm.dmbackend.domain.schedule.schedule.repository.ScheduleRepository;
import com.dm.dmbackend.domain.schedule.scheduleMessage.dto.ScheduleMessageRedisDto;
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
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String REDIS_KEY = "schedule:messages";

    // 1분마다 실행
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
                ScheduleMessageRedisDto dto = objectMapper.readValue(json, ScheduleMessageRedisDto.class);
                Schedule schedule = scheduleRepository.findById(dto.getScheduleId()).orElseThrow();
                Member member = memberRepository.findById(dto.getMemberId()).orElseThrow();
                // 일정 알림 생성 완료
                Notification notification = Notification.builder()
                        .senderId(member.getId())
                        .senderNickname(member.getNickname())
                        .senderProfileUrl(member.getProfileImageUrl())
                        .receiverId(member.getId())
                        .objectId(dto.getId())
                        .content(dto.getMessage())
                        .targetObject(Notification.TargetObject.SCHEDULE)
                        .build();
                schedule.setNotified(true);
                scheduleRepository.save(schedule);
                // 일정 알림 이벤트 생성 완료
                String kafkaMessage = objectMapper.writeValueAsString(notification);
                kafkaTemplate.send("schedule-topic", kafkaMessage);
                // 발송 완료된 메시지는 Redis에서 제거
                redisTemplate.opsForZSet().remove(REDIS_KEY, json);
                log.info("Sent scheduled message to Kafka. scheduleId={}", dto.getId());
            } catch (Exception e) {
                log.error("Failed to process scheduled message: {}", e.getMessage());
            }
        }
    }
}
