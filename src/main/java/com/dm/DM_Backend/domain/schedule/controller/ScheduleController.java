package com.dm.DM_Backend.domain.schedule.controller;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUser;
import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.schedule.dto.req.RequestDto;
import com.dm.DM_Backend.domain.schedule.dto.req.ScheduleUpdateDto;
import com.dm.DM_Backend.domain.schedule.dto.res.ResponseDto;
import com.dm.DM_Backend.domain.schedule.dto.res.ScheduleResponseDto;
import com.dm.DM_Backend.domain.schedule.entity.Schedule;
import com.dm.DM_Backend.domain.schedule.service.ScheduleService;
import com.dm.DM_Backend.domain.schedule.servicelmpl.ScheduleServicelmpl;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/rest-api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleServicelmpl scheduleServicelmpl;

    @PostMapping
    public ResponseEntity<ResponseDto> createSchedule(@RequestBody RequestDto requestDto,
                                                      @LoginUser LoginUserDto loginUser) {

        //서비스가 반환한 생성된 Schedule 엔티티 저장
        Schedule createdSchedule = scheduleServicelmpl.createSchedule(requestDto, loginUser.getId());

        ResponseDto responseDto = new ResponseDto(createdSchedule);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponseDto> findschedule(@PathVariable Long scheduleId) {

        ScheduleResponseDto scheduleResponseDto = scheduleServicelmpl.findScheduleById(scheduleId);

        return ResponseEntity.ok(scheduleResponseDto);
    }

    @PatchMapping("/{scheduleId}")
    public ResponseEntity<String> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleUpdateDto updateDto,
            @LoginUser LoginUserDto loginUser
    ) {
        scheduleServicelmpl.update(scheduleId, updateDto, loginUser.getId());
        return ResponseEntity.ok("일정이 성공적으로 수정되었습니다.");
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<String> deleteSchedule(
            @PathVariable Long scheduleId,
            @LoginUser LoginUserDto loginUser
    ) {
        scheduleServicelmpl.delete(scheduleId, loginUser.getId());
        return ResponseEntity.ok("일정이 성공적으로 삭제되었습니다.");
    }

    @GetMapping("/date")
    public ResponseEntity<List<ResponseDto>>findSchedulesByDate(@RequestParam("date")
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){

        List<ResponseDto> schedules = scheduleServicelmpl.getSchedulesForDate(date);

        return ResponseEntity.ok(schedules);
    }

}
