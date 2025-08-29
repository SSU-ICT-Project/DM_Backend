package com.dm.dmbackend.domain.schedule.schedule.dto.res;

import com.dm.dmbackend.global.common.vo.Location;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResponse {
    private Long id;
    private String scheduleName;
    private LocalDateTime scheduleStartTime;
    private LocalDateTime scheduleEndTime;
    private Location location;
    private String memo;
    private boolean d_Day;
    private boolean autoTimeCheck;
}
