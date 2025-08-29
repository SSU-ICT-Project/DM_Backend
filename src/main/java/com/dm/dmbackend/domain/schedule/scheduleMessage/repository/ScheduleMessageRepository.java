package com.dm.dmbackend.domain.schedule.scheduleMessage.repository;

import com.dm.dmbackend.domain.schedule.scheduleMessage.entity.ScheduleMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleMessageRepository extends JpaRepository<ScheduleMessage, Long> {
}
