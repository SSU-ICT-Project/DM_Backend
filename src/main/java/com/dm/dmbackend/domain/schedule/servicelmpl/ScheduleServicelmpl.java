package com.dm.dmbackend.domain.schedule.servicelmpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.repository.MemberRepository;
import com.dm.dmbackend.domain.schedule.dto.req.RequestDto;
import com.dm.dmbackend.domain.schedule.dto.req.ScheduleUpdateDto;
import com.dm.dmbackend.domain.schedule.dto.res.ResponseDto;
import com.dm.dmbackend.domain.schedule.dto.res.ScheduleResponseDto;
import com.dm.dmbackend.domain.schedule.entity.Schedule;
import com.dm.dmbackend.domain.schedule.repository.ScheduleRepository;
import com.dm.dmbackend.domain.schedule.service.ScheduleService;
import com.dm.dmbackend.global.common.response.DMPage;
import com.dm.dmbackend.global.exception.ReturnCode;
import com.dm.dmbackend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;


@Service
@RequiredArgsConstructor
public class ScheduleServicelmpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;



    @Transactional
    public Schedule createSchedule(RequestDto requestDto, Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));

        Schedule schedule = requestDto.ConvertToEntity(member);

        return scheduleRepository.save(schedule);

    }
    @Transactional(readOnly = true)
    public ScheduleResponseDto findScheduleById(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ServiceException(ReturnCode.SCHEDULE_NOT_FOUND));

        return new ScheduleResponseDto(schedule);
    }

    @Transactional
    public void update(Long scheduleId, ScheduleUpdateDto updateDto, Long userId) {
        // DB에서 수정할 Schedule 엔티티를 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ServiceException(ReturnCode.SCHEDULE_NOT_FOUND));

        // 이 일정을 수정할 권한이 있는지 확인
        if (!schedule.getMember().getId().equals(userId)) {
            throw new ServiceException(ReturnCode.UNAUTHORIZED_ACCESS);
        }

        // 엔티티의 데이터를 DTO의 내용으로 변경
        schedule.setScheduleName(updateDto.getScheduleName());
        schedule.setScheduleStartTime(updateDto.getScheduleStartTime());
        schedule.setScheduleEndTime(updateDto.getScheduleEndTime());
        schedule.setLocation(updateDto.getLocation());
        schedule.setMemo(updateDto.getMemo());
        schedule.setD_Day(updateDto.isD_Day());
        schedule.setAutoTimeCheck(updateDto.isAutoTimeCheck());

        //JPA가 알아서 변경된 내용을 감지, 수정

    }

    @Transactional
    public void delete(Long scheduleId, Long userId) {
        // DB에서 삭제할 Schedule 엔티티를 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ServiceException(ReturnCode.SCHEDULE_NOT_FOUND));

        //권한 확인
        if (!schedule.getMember().getId().equals(userId)) {
            throw new ServiceException(ReturnCode.UNAUTHORIZED_ACCESS);
        }
        scheduleRepository.delete(schedule);
    }

    @Transactional
    public DMPage<ResponseDto> getSchedulesForDate(LocalDate localDate, Long userId, Pageable pageable) {

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));

        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);


        Page<Schedule> schedules = scheduleRepository.findByMemberAndScheduleStartTimeBetweenOrderByScheduleStartTimeAsc(member,startOfDay, endOfDay,pageable);

        Page<ResponseDto> dtoPage = schedules.map(ResponseDto::new);


        return DMPage.of(dtoPage);
    }

    @Transactional
    public DMPage<ResponseDto> getSchedulesForMonth(YearMonth yearMonth, Long userId,Pageable pageable) {

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));

        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        Page<Schedule> schedules = scheduleRepository.findByMemberAndScheduleStartTimeBetweenOrderByScheduleStartTimeAsc(member,startOfMonth, endOfMonth,pageable);
        Page<ResponseDto> dtoPage = schedules.map(ResponseDto::new);

        return DMPage.of(dtoPage);
    }

    @Override
    @Transactional
    public void getPrepareMessage(LoginUserDto loginUser){

    }
}
