package com.dm.dmbackend.domain.schedule.schedule.servicelmpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.repository.MemberRepository;
import com.dm.dmbackend.domain.schedule.schedule.dto.req.ScheduleRequest;
import com.dm.dmbackend.domain.schedule.schedule.dto.res.ScheduleResponse;
import com.dm.dmbackend.domain.schedule.schedule.entity.Schedule;
import com.dm.dmbackend.domain.schedule.schedule.entity.SchedulePage;
import com.dm.dmbackend.domain.schedule.schedule.repository.ScheduleRepository;
import com.dm.dmbackend.domain.schedule.schedule.service.ScheduleService;
import com.dm.dmbackend.domain.schedule.scheduleMessage.service.ScheduleMessageService;
import com.dm.dmbackend.global.exception.ReturnCode;
import com.dm.dmbackend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServicelmpl implements ScheduleService {
    private final ScheduleMessageService scheduleMessageService;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;

    // 일정 생성
    @Override
    @Transactional
    public void createSchedule(ScheduleRequest scheduleRequest, LoginUserDto loginUser) {
        Schedule schedule = Schedule.builder()
                .member(loginUser.ConvertToMember())
                .scheduleName(scheduleRequest.getScheduleName())
                .scheduleStartTime(scheduleRequest.getScheduleStartTime())
                .scheduleEndTime(scheduleRequest.getScheduleEndTime())
                .location(scheduleRequest.getLocation())
                .memo(scheduleRequest.getMemo())
                .d_Day(scheduleRequest.getD_Day())
                .autoTimeCheck(scheduleRequest.getAutoTimeCheck())
                .build();
        scheduleRepository.save(schedule);

        // 자동시간계산 설정한 경우에만 예약 생성
        if (schedule.isAutoTimeCheck()){
            // 일정 알림 메시지 생성 요청
            String message = "";

            LocalDateTime scheduleTime = null;

            // 일정 알림 예약 생성
            scheduleMessageService.createScheduleMessage(schedule, message, scheduleTime, loginUser);
        }

        // 일정 알림 예약 생성 완료
        schedule.setNotified(true);
        scheduleRepository.save(schedule);
    }

    // 일정 상세 조회
    @Override
    @Transactional(readOnly = true)
    public ScheduleResponse findScheduleById(Long scheduleId,  LoginUserDto loginUser) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ServiceException(ReturnCode.SCHEDULE_NOT_FOUND));
        // 권한 확인
        if (!schedule.getMember().getId().equals(loginUser.getId())) {
            throw new ServiceException(ReturnCode.UNAUTHORIZED_ACCESS);
        }
        return convertToScheduleResponse(schedule);
    }

    // 일정 수정
    @Override
    @Transactional
    public void updateSchedule(Long scheduleId, ScheduleRequest scheduleRequest, LoginUserDto loginUser) {
        // DB에서 수정할 Schedule 엔티티를 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ServiceException(ReturnCode.SCHEDULE_NOT_FOUND));
        // 권한 확인
        if (!schedule.getMember().getId().equals(loginUser.getId())) {
            throw new ServiceException(ReturnCode.UNAUTHORIZED_ACCESS);
        }
        if (scheduleRequest.getScheduleName() != null) {
            schedule.setScheduleName(scheduleRequest.getScheduleName());
        }
        if (scheduleRequest.getScheduleStartTime() != null) {
            schedule.setScheduleStartTime(scheduleRequest.getScheduleStartTime());
        }
        if (scheduleRequest.getScheduleEndTime() != null) {
            schedule.setScheduleEndTime(scheduleRequest.getScheduleEndTime());
        }
        if (scheduleRequest.getLocation() != null) {
            schedule.setLocation(scheduleRequest.getLocation());
        }
        if (scheduleRequest.getMemo() != null) {
            schedule.setMemo(scheduleRequest.getMemo());
        }
        if (scheduleRequest.getD_Day() != null) {
            schedule.setD_Day(scheduleRequest.getD_Day());
        }
        if (scheduleRequest.getAutoTimeCheck() != null) {
            schedule.setAutoTimeCheck(scheduleRequest.getAutoTimeCheck());
        }
        scheduleRepository.save(schedule);
    }

    // 일정 삭제
    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId, LoginUserDto loginUser) {
        // DB에서 삭제할 Schedule 엔티티를 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ServiceException(ReturnCode.SCHEDULE_NOT_FOUND));
        // 권한 확인
        if (!schedule.getMember().getId().equals(loginUser.getId())) {
            throw new ServiceException(ReturnCode.UNAUTHORIZED_ACCESS);
        }
        scheduleRepository.delete(schedule);
    }

    // 날짜별 일정 조회
    @Override
    @Transactional
    public Page<ScheduleResponse> getSchedulesForDate(LocalDate localDate, LoginUserDto loginUser, Pageable pageable) {
        Member member = memberRepository.findById(loginUser.getId())
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        checkPageSize(pageable.getPageSize());
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        Page<Schedule> schedulePage = scheduleRepository.findByMemberAndScheduleStartTimeBetweenOrderByScheduleStartTimeAsc(member,startOfDay, endOfDay, pageable);
        return schedulePage.map(this::convertToScheduleResponse);
    }

    // 월별 일정 조회
    @Override
    @Transactional
    public Page<ScheduleResponse> getSchedulesForMonth(YearMonth yearMonth, LoginUserDto loginUserDto,Pageable pageable) {
        Member member = memberRepository.findById(loginUserDto.getId())
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        checkPageSize(pageable.getPageSize());
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        Page<Schedule> schedulePage = scheduleRepository.findByMemberAndScheduleStartTimeBetweenOrderByScheduleStartTimeAsc(member,startOfMonth, endOfMonth,pageable);
        return schedulePage.map(this::convertToScheduleResponse);
    }

    // ----------------- 헬퍼 메서드 -----------------

    // 요청 페이지 수 제한
    private void checkPageSize(int pageSize) {
        int maxPageSize = SchedulePage.getMaxPageSize();
        if (pageSize > maxPageSize) {
            throw new ServiceException(ReturnCode.PAGE_REQUEST_FAIL);
        }
    }

    // Schedule를 ScheduleResponse로 변환
    private ScheduleResponse convertToScheduleResponse(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .scheduleName(schedule.getScheduleName())
                .scheduleStartTime(schedule.getScheduleStartTime())
                .scheduleEndTime(schedule.getScheduleEndTime())
                .location(schedule.getLocation())
                .memo(schedule.getMemo())
                .d_Day(schedule.isD_Day())
                .autoTimeCheck(schedule.isAutoTimeCheck())
                .build();
    }
}
