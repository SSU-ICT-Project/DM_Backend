package com.dm.dmbackend.domain.schedule.dto.req;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleUpdateDto {

    private String scheduleName;
    private LocalDateTime scheduleStartTime;
    private LocalDateTime scheduleEndTime;
    private String location;
    private String memo;
    private boolean d_Day;
    private boolean autoTimeCheck;
}
