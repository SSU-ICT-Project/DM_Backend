package com.dm.dmbackend.domain.schedule.schedule.dto.external.req;

import com.dm.dmbackend.global.common.vo.Location;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleMessageRequest {
    private String scheduleName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduleStartTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduleEndTime;

    @JsonProperty("DepartureLocation")  // FastAPI에서 요구하는 이름과 일치
    private Location DepartureLocation;

    @JsonProperty("ArrivalLocation")   // FastAPI에서 요구하는 이름과 일치
    private Location ArrivalLocation;
}
