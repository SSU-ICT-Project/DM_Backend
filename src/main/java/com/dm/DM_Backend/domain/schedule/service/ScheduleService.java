package com.dm.DM_Backend.domain.schedule.service;


import com.dm.DM_Backend.domain.schedule.dto.req.RequestDto;
import com.dm.DM_Backend.domain.schedule.dto.req.ScheduleUpdateDto;
import com.dm.DM_Backend.domain.schedule.dto.res.ResponseDto;
import com.dm.DM_Backend.domain.schedule.dto.res.ScheduleResponseDto;
import com.dm.DM_Backend.domain.schedule.entity.Schedule;
import com.dm.DM_Backend.global.common.response.DMPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface ScheduleService  {

    Schedule createSchedule(RequestDto requestDto, Long userId);
    ScheduleResponseDto findScheduleById(Long scheduleId);
    void update(Long scheduleId, ScheduleUpdateDto updateDto, Long userId);
    void delete(Long scheduleId, Long userId);
    DMPage<ResponseDto> getSchedulesForDate(LocalDate localDate, Long userId, Pageable pageable);
    DMPage<ResponseDto> getSchedulesForMonth(YearMonth yearMonth,Long userId, Pageable pageable);

}
