package com.dm.dmbackend.domain.schedule.scheduleMessage.serviceImpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.schedule.schedule.entity.Schedule;
import com.dm.dmbackend.domain.schedule.scheduleMessage.entity.ScheduleMessage;
import com.dm.dmbackend.domain.schedule.scheduleMessage.repository.ScheduleMessageRepository;
import com.dm.dmbackend.domain.schedule.scheduleMessage.service.ScheduleMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleMessageServiceImpl implements ScheduleMessageService {
    private final ScheduleMessageRepository scheduleMessageRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_KEY = "schedule:messages";

    // 일정 알림 예약 생성
    @Override
    @Transactional
    public void createScheduleMessage(Schedule schedule, String message, LocalDateTime scheduleTime, LoginUserDto loginUser) {
        ScheduleMessage scheduleMessage = ScheduleMessage.builder()
                .member(loginUser.ConvertToMember())
                .schedule(schedule)
                .message(message)
                .scheduleTime(scheduleTime)
                .build();
        scheduleMessageRepository.save(scheduleMessage);
        // Redis ZSET에 ID만 저장 (score = epoch milli)
        double score = scheduleTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String value = String.valueOf(scheduleMessage.getId());
        redisTemplate.opsForZSet().add(REDIS_KEY, value, score);
        log.info("ScheduleMessage id saved to Redis ZSET. scheduleMessageId={}, score={}", scheduleMessage.getId(), score);
    }
}
