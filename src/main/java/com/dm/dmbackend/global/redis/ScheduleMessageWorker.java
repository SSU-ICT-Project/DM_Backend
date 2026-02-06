package com.dm.dmbackend.global.redis;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.repository.MemberRepository;
import com.dm.dmbackend.domain.notification.dto.NotificationPayload;
import com.dm.dmbackend.domain.notification.factory.NotificationPayloadFactory;
import com.dm.dmbackend.domain.schedule.schedule.entity.Schedule;
import com.dm.dmbackend.domain.schedule.schedule.repository.ScheduleRepository;
import com.dm.dmbackend.domain.schedule.scheduleMessage.dto.ScheduleMessageRedisDto;
import com.dm.dmbackend.domain.schedule.scheduleMessage.entity.ScheduleMessage;
import com.dm.dmbackend.domain.schedule.scheduleMessage.repository.ScheduleMessageRepository;
import com.dm.dmbackend.global.kafka.event.notification.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.dm.dmbackend.global.constants.KafkaKey.SCHEDULE_NOTIFICATION_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleMessageWorker {
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final ScheduleMessageRepository scheduleMessageRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationEventPublisher notificationEventPublisher;
    private static final String REDIS_KEY = "schedule:messages";

    // 1분마다 실행
    @Scheduled(cron = "0 * * * * *")
    public void processDueMessages() {
        long now = Instant.now().toEpochMilli();
        long window = 60 * 1000; // 1분
        long maxScore = now + window;

        // Redis ZSET에서 지금 보낼 메시지 조회
        Set<Object> dueMessageIds = redisTemplate.opsForZSet().rangeByScore(REDIS_KEY, 0, maxScore);
        if (dueMessageIds == null || dueMessageIds.isEmpty()) {
            return;
        }
        List<Long> ids = new ArrayList<>(dueMessageIds.size());
        for (Object idObj : dueMessageIds) {
            try {
                ids.add(Long.valueOf(String.valueOf(idObj)));
            } catch (NumberFormatException e) {
                log.warn("Invalid scheduleMessageId in Redis ZSET: {}", idObj);
            }
        }
        if (ids.isEmpty()) {
            return;
        }
        List<ScheduleMessage> scheduleMessages = scheduleMessageRepository.findAllById(ids);
        for (ScheduleMessage scheduleMessage : scheduleMessages) {
            try {
                Long scheduleId = scheduleMessage.getSchedule().getId();
                Long memberId = scheduleMessage.getMember().getId();
                Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();
                Member member = memberRepository.findById(memberId).orElseThrow();
                ScheduleMessageRedisDto dto = ScheduleMessageRedisDto.builder()
                        .id(scheduleMessage.getId())
                        .message(scheduleMessage.getMessage())
                        .build();
                // 일정 알림 생성 완료
                NotificationPayload payload = NotificationPayloadFactory.scheduleMessage(member, dto);
                schedule.setNotified(true);
                scheduleRepository.save(schedule);
                // 일정 알림 이벤트 생성 완료
                notificationEventPublisher.publishNotification(payload, SCHEDULE_NOTIFICATION_TOPIC);
                // 발송 완료된 메시지는 Redis에서 제거
                redisTemplate.opsForZSet().remove(REDIS_KEY, String.valueOf(scheduleMessage.getId()));
                log.info("Published scheduled notification event. scheduleMessageId={}", scheduleMessage.getId());
            } catch (Exception e) {
                log.error("Failed to process scheduled message: {}", e.getMessage());
            }
        }
    }
}
