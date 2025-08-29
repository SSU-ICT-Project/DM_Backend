package com.dm.dmbackend.domain.schedule.schedule.controller;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUser;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.schedule.schedule.dto.req.ScheduleRequest;
import com.dm.dmbackend.domain.schedule.schedule.dto.res.ScheduleResponse;
import com.dm.dmbackend.domain.schedule.schedule.entity.SchedulePage;
import com.dm.dmbackend.domain.schedule.schedule.servicelmpl.ScheduleServicelmpl;
import com.dm.dmbackend.global.common.response.ApiResponse;
import com.dm.dmbackend.global.common.response.DMPage;
import com.dm.dmbackend.global.exception.ReturnCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/rest-api/v1/schedule")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "일정 API")
public class ScheduleController {
    private final ScheduleServicelmpl scheduleService;
    private final ScheduleServicelmpl scheduleServicelmpl;

    // 일정 생성
    @PostMapping
    @Operation(summary = "일정 생성")
    public ApiResponse<String> createSchedule(@RequestBody ScheduleRequest scheduleRequest, @LoginUser LoginUserDto loginUser) {
        scheduleService.createSchedule(scheduleRequest, loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 일정 상세 조회
    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 상세 조회")
    public ApiResponse<ScheduleResponse> findschedule(@PathVariable Long scheduleId, @LoginUser LoginUserDto loginUser) {
        return ApiResponse.of(scheduleService.findScheduleById(scheduleId, loginUser));
    }

    // 일정 수정
    @PatchMapping("/{scheduleId}")
    @Operation(summary = "일정 수정")
    public ApiResponse<String> updateSchedule(@PathVariable Long scheduleId, @RequestBody ScheduleRequest scheduleRequest,
                                              @LoginUser LoginUserDto loginUser) {
        scheduleService.updateSchedule(scheduleId, scheduleRequest, loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 일정 삭제
    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "일정 삭제")
    public ApiResponse<String> deleteSchedule(@PathVariable Long scheduleId, @LoginUser LoginUserDto loginUser) {
        scheduleServicelmpl.deleteSchedule(scheduleId, loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 날짜별 일정 조회
    @GetMapping("/date")
    @Operation(summary = "날짜별 일정 조회")
    public ApiResponse<ScheduleResponse>findSchedulesByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                         LocalDate date, @LoginUser LoginUserDto loginUser,
                                                            @ModelAttribute SchedulePage schedulePage) {
        Pageable pageable = PageRequest.of(schedulePage.getPage(), schedulePage.getSize());
        return ApiResponse.of(DMPage.of(scheduleService.getSchedulesForDate(date, loginUser, pageable)));
    }

    // 월별 일정 조회
    @GetMapping("/month")
    @Operation(summary = "월별 일정 조회")
    public ApiResponse<ScheduleResponse>findSchedulesByMonth(@RequestParam("yearMonth") @DateTimeFormat(pattern = "yyyy-MM")
                                                            YearMonth yearMonth, @LoginUser LoginUserDto loginUser,
                                                             @ModelAttribute SchedulePage schedulePage) {
        Pageable pageable = PageRequest.of(schedulePage.getPage(), schedulePage.getSize());
        return ApiResponse.of(DMPage.of(scheduleService.getSchedulesForMonth(yearMonth, loginUser, pageable)));
    }
}
