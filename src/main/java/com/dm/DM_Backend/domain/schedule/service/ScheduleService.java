package com.dm.DM_Backend.domain.schedule.service;


import com.dm.DM_Backend.domain.schedule.dto.req.RequestDto;
import com.dm.DM_Backend.domain.schedule.dto.req.ScheduleUpdateDto;
import com.dm.DM_Backend.domain.schedule.dto.res.ResponseDto;
import com.dm.DM_Backend.domain.schedule.dto.res.ScheduleResponseDto;
import com.dm.DM_Backend.domain.schedule.entity.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService  {

    Schedule createSchedule(RequestDto requestDto, Long userId);
    ScheduleResponseDto findScheduleById(Long scheduleId);
    void update(Long scheduleId, ScheduleUpdateDto updateDto, Long userId);
    void delete(Long scheduleId, Long userId);
    List<ResponseDto> getSchedulesForDate(LocalDate localDate);

}
