package com.dm.dmbackend.domain.schedule.scheduleMessage.service;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.schedule.schedule.entity.Schedule;

import java.time.LocalDateTime;

public interface ScheduleMessageService {
    // 일정 알림 예약 생성
    void createScheduleMessage(Schedule schedule, String message, LocalDateTime scheduleTime, LoginUserDto loginUser);
}
