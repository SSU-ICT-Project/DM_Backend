package com.dm.dmbackend.domain.schedule.schedule.service;


import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.schedule.schedule.dto.req.ScheduleRequest;
import com.dm.dmbackend.domain.schedule.schedule.dto.res.ScheduleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;

public interface ScheduleService  {
    // 일정 생성
    void createSchedule(ScheduleRequest scheduleRequest, LoginUserDto loginUser);

    // 일정 상세 조회
    ScheduleResponse findScheduleById(Long scheduleId, LoginUserDto loginUser);

    // 일정 수정
    void updateSchedule(Long scheduleId, ScheduleRequest scheduleRequest, LoginUserDto loginUser);

    // 일정 삭제
    void deleteSchedule(Long scheduleId, LoginUserDto loginUser);

    // 날짜별 일정 조회
    Page<ScheduleResponse> getSchedulesForDate(LocalDate localDate, LoginUserDto loginUser, Pageable pageable);

    // 월별 일정 조회
    Page<ScheduleResponse> getSchedulesForMonth(YearMonth yearMonth, LoginUserDto loginUser, Pageable pageable);
}
