package com.dm.DM_Backend.domain.schedule.scheduler;

import com.dm.DM_Backend.domain.schedule.entity.Schedule;
import com.dm.DM_Backend.domain.schedule.repository.ScheduleRepository;
import com.dm.DM_Backend.domain.schedule.servicelmpl.AgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleNotificationScheduler {

    private final ScheduleRepository scheduleRepository;
    private final AgentService agentService;

    // 매 5분마다 실행 (예: 09:00, 09:05, 09:10 ...)
    @Scheduled(cron = "0 */5 * * * *")
    public void triggerSmartNotification() {
        log.info("스마트 알림 스케줄러 실행: {}", LocalDateTime.now());

        // 1. 지금부터 3시간 후에 시작하고, 아직 알림이 가지 않은 일정들을 DB에서 찾습니다.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeHoursLater = now.plusHours(3);
        List<Schedule> upcomingSchedules = scheduleRepository
                .findAllByScheduleStartTimeBetweenAndNotifiedIsFalseAndAutoTimeCheckIsTrue(now, threeHoursLater);

        if (upcomingSchedules.isEmpty()) {
            log.info("알림을 보낼 대상 일정이 없습니다.");
            return;
        }

        log.info("총 {}개의 일정에 대한 스마트 알림 생성을 시작합니다.", upcomingSchedules.size());

        // 2. 각 일정에 대해 에이전트 서비스를 호출합니다.
        for (Schedule schedule : upcomingSchedules) {
            try {
                agentService.calculateAndNotify(schedule);
            } catch (Exception e) {
                // 특정 일정에서 에러가 나도 다른 일정 처리에 영향이 없도록 처리
                log.error("ID {} 일정 알림 처리 중 에러 발생: {}", schedule.getId(), e.getMessage());
            }
        }
    }
}



