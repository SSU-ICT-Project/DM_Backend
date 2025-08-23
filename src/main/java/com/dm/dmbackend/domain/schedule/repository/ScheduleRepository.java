package com.dm.dmbackend.domain.schedule.repository;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.schedule.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Page<Schedule> findByMemberAndScheduleStartTimeBetweenOrderByScheduleStartTimeAsc(Member member, LocalDateTime scheduleStartTime, LocalDateTime scheduleStartTime2, Pageable pageable);

}
