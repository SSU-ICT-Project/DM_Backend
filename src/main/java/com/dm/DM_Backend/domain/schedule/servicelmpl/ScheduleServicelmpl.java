package com.dm.DM_Backend.domain.schedule.servicelmpl;

import com.dm.DM_Backend.domain.account.member.entity.Member;
import com.dm.DM_Backend.domain.account.member.repository.MemberRepository;
import com.dm.DM_Backend.domain.schedule.dto.req.RequestDto;
import com.dm.DM_Backend.domain.schedule.dto.req.ScheduleUpdateDto;
import com.dm.DM_Backend.domain.schedule.dto.res.ResponseDto;
import com.dm.DM_Backend.domain.schedule.dto.res.ScheduleResponseDto;
import com.dm.DM_Backend.domain.schedule.entity.Schedule;
import com.dm.DM_Backend.domain.schedule.repository.ScheduleRepository;
import com.dm.DM_Backend.domain.schedule.service.ScheduleService;
import com.dm.DM_Backend.global.exception.ReturnCode;
import com.dm.DM_Backend.global.exception.ServiceException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


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
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 일정을 찾을 수 없습니다: " + scheduleId));

        return new ScheduleResponseDto(schedule);
    }

    @Transactional
    public void update(Long scheduleId, ScheduleUpdateDto updateDto, Long userId) {
        // DB에서 수정할 Schedule 엔티티를 조회합니다.
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 일정을 찾을 수 없습니다: " + scheduleId));

        // 이 일정을 수정할 권한이 있는지 확인
        if (!schedule.getMember().getId().equals(userId)) {
            throw new SecurityException("일정을 수정할 권한이 없습니다.");
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
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 일정을 찾을 수 없습니다: " + scheduleId));

        //권한 확인
        if (!schedule.getMember().getId().equals(userId)) {
            throw new SecurityException("일정을 삭제할 권한이 없습니다.");
        }
        scheduleRepository.delete(schedule);
    }

    @Transactional
    public List<ResponseDto> getSchedulesForDate(LocalDate localDate) {

        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);


        List<Schedule> schedules = scheduleRepository.findByScheduleStartTimeBetweenOrderByScheduleStartTimeAsc(startOfDay, endOfDay);

        return schedules.stream()
                .map(ResponseDto::new)
                .collect(Collectors.toList());
    }
}
