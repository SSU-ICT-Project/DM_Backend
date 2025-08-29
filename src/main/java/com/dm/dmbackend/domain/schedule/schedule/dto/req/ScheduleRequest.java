package com.dm.dmbackend.domain.schedule.schedule.dto.req;

import com.dm.dmbackend.global.common.vo.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleRequest {
    private String scheduleName;
    private LocalDateTime scheduleStartTime;
    private LocalDateTime scheduleEndTime;
    private Location location;
    private String memo;
    private Boolean d_Day;
    private Boolean autoTimeCheck;
}
