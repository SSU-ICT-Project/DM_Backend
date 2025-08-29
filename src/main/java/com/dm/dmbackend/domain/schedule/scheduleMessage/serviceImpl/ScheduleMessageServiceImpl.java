package com.dm.dmbackend.domain.schedule.scheduleMessage.serviceImpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.schedule.schedule.entity.Schedule;
import com.dm.dmbackend.domain.schedule.scheduleMessage.entity.ScheduleMessage;
import com.dm.dmbackend.domain.schedule.scheduleMessage.repository.ScheduleMessageRepository;
import com.dm.dmbackend.domain.schedule.scheduleMessage.service.ScheduleMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;
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
        try {
            // JSON 직렬화
            String jsonValue = objectMapper.writeValueAsString(scheduleMessage);
            // Redis ZSET에 저장 (score = epoch milli)
            double score = scheduleTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            redisTemplate.opsForZSet().add(REDIS_KEY, jsonValue, score);
            log.info("ScheduleMessage saved to Redis ZSET. scheduleId={}, score={}", scheduleMessage.getId(), score);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ScheduleMessage: {}", e.getMessage());
        }
    }
}

