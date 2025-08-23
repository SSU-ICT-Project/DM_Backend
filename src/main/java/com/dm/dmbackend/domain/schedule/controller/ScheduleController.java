package com.dm.dmbackend.domain.schedule.controller;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUser;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.schedule.dto.req.RequestDto;
import com.dm.dmbackend.domain.schedule.dto.req.ScheduleUpdateDto;
import com.dm.dmbackend.domain.schedule.dto.res.ResponseDto;
import com.dm.dmbackend.domain.schedule.dto.res.ScheduleResponseDto;
import com.dm.dmbackend.domain.schedule.entity.Schedule;

import com.dm.dmbackend.domain.schedule.servicelmpl.ScheduleServicelmpl;
import com.dm.dmbackend.global.common.response.ApiResponse;
import com.dm.dmbackend.global.common.response.DMPage;
import com.dm.dmbackend.global.exception.ReturnCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    private final ScheduleServicelmpl scheduleServicelmpl;

    @PostMapping
    @Operation(summary = "일정 생성")
    public ApiResponse<ResponseDto> createSchedule(@RequestBody RequestDto requestDto,
                                                      @LoginUser LoginUserDto loginUser) {

        //서비스가 반환한 생성된 Schedule 엔티티 저장
        Schedule createdSchedule = scheduleServicelmpl.createSchedule(requestDto, loginUser.getId());


        return ApiResponse.of(ReturnCode.SUCCESS);

    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 상세 조회")
    public ApiResponse<ScheduleResponseDto> findschedule(@PathVariable Long scheduleId) {

        ScheduleResponseDto scheduleResponseDto = scheduleServicelmpl.findScheduleById(scheduleId);

        return ApiResponse.of(scheduleResponseDto);
    }

    @PatchMapping("/{scheduleId}")
    @Operation(summary = "일정 수정")
    public ApiResponse<String> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleUpdateDto updateDto,
            @LoginUser LoginUserDto loginUser
    ) {
        scheduleServicelmpl.update(scheduleId, updateDto, loginUser.getId());
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "일정 삭제")
    public ApiResponse<String> deleteSchedule(
            @PathVariable Long scheduleId,
            @LoginUser LoginUserDto loginUser
    ) {
        scheduleServicelmpl.delete(scheduleId, loginUser.getId());
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    @GetMapping("/date")
    @Operation(summary = "날짜별 일정 조회")
    public ApiResponse<ResponseDto>findSchedulesByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                         LocalDate date, @LoginUser LoginUserDto loginUser, Pageable pageable) {

        DMPage<ResponseDto> schedules = scheduleServicelmpl.getSchedulesForDate(date,loginUser.getId(),pageable);

        return ApiResponse.of(schedules);
    }

    @GetMapping("/month")
    @Operation(summary = "월별 일정 조회")
    public ApiResponse<ResponseDto>findSchedulesByMonth(@RequestParam("yearMonth") @DateTimeFormat(pattern = "yyyy-MM")
                                                            YearMonth yearMonth, @LoginUser LoginUserDto loginUser , Pageable pageable) {
        DMPage<ResponseDto> schedules = scheduleServicelmpl.getSchedulesForMonth(yearMonth,loginUser.getId(),pageable);

        return ApiResponse.of(schedules);
    }

}
