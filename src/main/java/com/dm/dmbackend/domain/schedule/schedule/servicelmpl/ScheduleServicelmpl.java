package com.dm.dmbackend.domain.schedule.schedule.servicelmpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.repository.MemberRepository;
import com.dm.dmbackend.domain.schedule.schedule.dto.external.req.ScheduleMessageRequest;
import com.dm.dmbackend.domain.schedule.schedule.dto.external.res.ScheduleMessageResponse;
import com.dm.dmbackend.domain.schedule.schedule.dto.req.ScheduleRequest;
import com.dm.dmbackend.domain.schedule.schedule.dto.res.ScheduleResponse;
import com.dm.dmbackend.domain.schedule.schedule.entity.Schedule;
import com.dm.dmbackend.domain.schedule.schedule.entity.SchedulePage;
import com.dm.dmbackend.domain.schedule.schedule.repository.ScheduleRepository;
import com.dm.dmbackend.domain.schedule.schedule.service.ScheduleService;
import com.dm.dmbackend.global.common.response.PageResponse;
import com.dm.dmbackend.domain.schedule.scheduleMessage.service.ScheduleMessageService;
import com.dm.dmbackend.global.exception.ReturnCode;
import com.dm.dmbackend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServicelmpl implements ScheduleService {
    private final ScheduleMessageService scheduleMessageService;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate;

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

        // 자동시간계산 설정한 경우에만 일정 알림 예약 생성
        if (schedule.isAutoTimeCheck()){
            // 일정 알림 메시지 생성 요청
            String url = "https://api.langgraph.letzgo.site/recommend";
            ScheduleMessageRequest req = convertToScheduleMessageRequest(schedule, loginUser);
            // POST 요청 보내기
            ScheduleMessageResponse res = restTemplate.postForObject(url, req, ScheduleMessageResponse.class);
            String message = res.getRecommendation();
            // 이동 시간 파싱
            Duration travelDuration = parseDurationFromMessage(message);
            // 메시지에서 [이동 시간: HH:MM] 제거
            String cleanedMessage = removeTravelDurationTag(message);
            // 기준 scheduleTime (출발 시간)
            LocalDateTime baseTime = schedule.getScheduleStartTime().minus(travelDuration);
            // 출발 1시간 전
            LocalDateTime scheduleTime1 = baseTime.minusHours(1);
            scheduleMessageService.createScheduleMessage(schedule, cleanedMessage, scheduleTime1, loginUser);
            // 출발 30분 전
            LocalDateTime scheduleTime2 = baseTime.minusMinutes(30);
            scheduleMessageService.createScheduleMessage(schedule, cleanedMessage, scheduleTime2, loginUser);
        }
    }

    // 일정 상세 조회
    @Override
    @Transactional(readOnly = true)
    public ScheduleResponse findScheduleById(Long scheduleId,  LoginUserDto loginUser) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ServiceException(ReturnCode.SCHEDULE_NOT_FOUND));
        // 권한 확인
        if (!schedule.getMember().getId().equals(loginUser.getId())) {
            throw new ServiceException(ReturnCode.UNAUTHORIZED_SCHEDULE_ACCESS);
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
            throw new ServiceException(ReturnCode.UNAUTHORIZED_SCHEDULE_ACCESS);
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
            throw new ServiceException(ReturnCode.UNAUTHORIZED_SCHEDULE_ACCESS);
        }
        scheduleRepository.delete(schedule);
    }

    // 날짜별 일정 조회
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ScheduleResponse> getSchedulesForDate(LocalDate localDate, LoginUserDto loginUser, Pageable pageable) {
        Member member = memberRepository.findById(loginUser.getId())
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        checkPageSize(pageable.getPageSize());
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        Page<Schedule> schedulePage = scheduleRepository.findByMemberAndScheduleStartTimeBetweenOrderByScheduleStartTimeAsc(member,startOfDay, endOfDay, pageable);
        return PageResponse.of(schedulePage.map(this::convertToScheduleResponse));
    }

    // 월별 일정 조회
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ScheduleResponse> getSchedulesForMonth(YearMonth yearMonth, LoginUserDto loginUserDto,Pageable pageable) {
        Member member = memberRepository.findById(loginUserDto.getId())
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        checkPageSize(pageable.getPageSize());
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        Page<Schedule> schedulePage = scheduleRepository.findByMemberAndScheduleStartTimeBetweenOrderByScheduleStartTimeAsc(member,startOfMonth, endOfMonth,pageable);
        return PageResponse.of(schedulePage.map(this::convertToScheduleResponse));
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

    // Schedule를 ScheduleMessageRequest로 변환
    private ScheduleMessageRequest convertToScheduleMessageRequest(Schedule schedule, LoginUserDto loginUser) {
        return ScheduleMessageRequest.builder()
                .scheduleName(schedule.getScheduleName())
                .scheduleStartTime(schedule.getScheduleStartTime())
                .scheduleEndTime(schedule.getScheduleEndTime())
                .DepartureLocation(loginUser.getLocation())
                .ArrivalLocation(schedule.getLocation())
                .build();
    }

    // message에서 총 소요시간 추출
    private Duration parseDurationFromMessage(String message) {
        // 정규식: "[이동 시간: HH:MM]" 형태 추출 (뒤에 공백/개행 허용)
        Pattern pattern = Pattern.compile("\\[이동 시간:\\s*(\\d{2}):(\\d{2})]\\s*$");
        Matcher matcher = pattern.matcher(message.trim());
        if (matcher.find()) {
            int hours = Integer.parseInt(matcher.group(1));
            int minutes = Integer.parseInt(matcher.group(2));
            return Duration.ofHours(hours).plusMinutes(minutes);
        }
        return Duration.ZERO; // 못 찾으면 0분
    }

    // message에서 [이동 시간: HH:MM] 꼬리표 제거
    private String removeTravelDurationTag(String message) {
        return message.replaceAll("\\s*\\[이동 시간:\\s*\\d{2}:\\d{2}]\\s*$", "").trim();
    }

}
