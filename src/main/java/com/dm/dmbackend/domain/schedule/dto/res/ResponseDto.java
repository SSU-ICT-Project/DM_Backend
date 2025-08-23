package com.dm.dmbackend.domain.schedule.dto.res;

import com.dm.dmbackend.domain.schedule.entity.Schedule;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class ResponseDto {

    private Long scheduleId;
    private String scheduleName;
    private LocalDateTime scheduleStartTime;
    private LocalDateTime scheduleEndTime;
    private String location;
    private String memo;
    private boolean d_Day;
    private boolean autoTimeCheck;

    public ResponseDto(Schedule schedule) {
        this.scheduleId = schedule.getId(); // BaseEntity로부터 상속받은 id
        this.scheduleName = schedule.getScheduleName();
        this.scheduleStartTime = schedule.getScheduleStartTime();
        this.scheduleEndTime = schedule.getScheduleEndTime();
        this.location = schedule.getLocation();
        this.memo = schedule.getMemo();
        this.d_Day = schedule.isD_Day();
        this.autoTimeCheck = schedule.isAutoTimeCheck();
    }
}
