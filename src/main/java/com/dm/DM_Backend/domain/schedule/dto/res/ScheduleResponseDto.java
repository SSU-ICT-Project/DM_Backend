package com.dm.DM_Backend.domain.schedule.dto.res;


import com.dm.DM_Backend.domain.schedule.entity.Schedule;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ScheduleResponseDto {

    private final Long scheduleId;
    private final String scheduleName;
    private final LocalDateTime scheduleStartTime;
    private final LocalDateTime scheduleEndTime;
    private final String memo;
    private final boolean d_Day;
    private final boolean autoTimeCheck;

    // Schedule 엔티티를 받아 DTO로 변환하는 생성자
    public ScheduleResponseDto(Schedule schedule) {
        this.scheduleId = schedule.getId();
        this.scheduleName = schedule.getScheduleName();
        this.scheduleStartTime = schedule.getScheduleStartTime();
        this.scheduleEndTime = schedule.getScheduleEndTime();
        this.memo = schedule.getMemo();
        this.d_Day = schedule.isD_Day();
        this.autoTimeCheck = schedule.isAutoTimeCheck();
    }
}
