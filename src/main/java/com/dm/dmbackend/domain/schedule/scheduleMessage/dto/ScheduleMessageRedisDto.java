package com.dm.dmbackend.domain.schedule.scheduleMessage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleMessageRedisDto {
    private Long id;               // scheduleMessage id
    private Long scheduleId;       // schedule id
    private Long memberId;         // member id
    private String message;        // 알림 메시지 내용
    private LocalDateTime scheduleTime; // 알림 예정 시간
}
