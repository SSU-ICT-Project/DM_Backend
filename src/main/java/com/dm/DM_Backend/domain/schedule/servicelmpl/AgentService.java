package com.dm.DM_Backend.domain.schedule.servicelmpl;

import com.dm.DM_Backend.domain.account.member.entity.Member;
import com.dm.DM_Backend.domain.fcm.dto.FcmMessage;
import com.dm.DM_Backend.domain.fcm.service.FcmService;
import com.dm.DM_Backend.domain.notification.entity.Notification;
import com.dm.DM_Backend.domain.schedule.entity.Schedule;
import com.dm.DM_Backend.global.external.maps.MapApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final MapApiService mapsApiService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ChatModel chatModel;
    private String HomeAddress;
    private String ScheduleAddress;


    @Transactional // 알림 상태(notified)를 변경하므로 트랜잭션 처리가 필요합니다.
    public void calculateAndNotify(Schedule schedule) {
        Member member = schedule.getMember();

        //지도 API를 호출하여 이동 시간을 계산

        HomeAddress = member.getLatitude() + "," + member.getLongitude();
        ScheduleAddress = schedule.getLatitude() + "," + schedule.getLongitude();
        log.info(member.getLatitude(), member.getLongitude());
        log.info(schedule.getMember().getLatitude(), schedule.getMember().getLongitude());
        log.info(HomeAddress, ScheduleAddress);
        int travelTime = mapsApiService.getTravelTimeInMinutes(
                HomeAddress,
                ScheduleAddress,
                schedule.getScheduleStartTime()
        );

        log.info("[ID: {}] 이동 시간 계산 완료: {}분, {}", schedule.getId(), travelTime);




    }
}