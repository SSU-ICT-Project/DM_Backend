package com.dm.dmbackend.domain.schedule.service;


import com.dm.dmbackend.domain.schedule.dto.req.RequestDto;
import com.dm.dmbackend.domain.schedule.dto.req.ScheduleUpdateDto;
import com.dm.dmbackend.domain.schedule.dto.res.ResponseDto;
import com.dm.dmbackend.domain.schedule.dto.res.ScheduleResponseDto;
import com.dm.dmbackend.domain.schedule.entity.Schedule;
import com.dm.dmbackend.global.common.response.DMPage;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;

public interface ScheduleService  {

    Schedule createSchedule(RequestDto requestDto, Long userId);
    ScheduleResponseDto findScheduleById(Long scheduleId);
    void update(Long scheduleId, ScheduleUpdateDto updateDto, Long userId);
    void delete(Long scheduleId, Long userId);
    DMPage<ResponseDto> getSchedulesForDate(LocalDate localDate, Long userId, Pageable pageable);
    DMPage<ResponseDto> getSchedulesForMonth(YearMonth yearMonth,Long userId, Pageable pageable);

}
