package com.dm.DM_Backend.domain.schedule.repository;

import com.dm.DM_Backend.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByScheduleStartTimeBetweenOrderByScheduleStartTimeAsc(LocalDateTime startOfDay, LocalDateTime endOfDay);

}
