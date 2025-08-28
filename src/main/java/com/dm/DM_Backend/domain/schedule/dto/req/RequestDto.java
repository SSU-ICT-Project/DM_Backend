package com.dm.DM_Backend.domain.schedule.dto.req;

import com.dm.DM_Backend.domain.account.member.entity.Member;
import com.dm.DM_Backend.domain.schedule.entity.Schedule;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RequestDto {

    private String scheduleName;
    private LocalDateTime scheduleStartTime;
    private LocalDateTime scheduleEndTime;
    private String placeName;
    private String PlaceAddress;
    private String latitude;
    private String longitude;
    private String memo;
    private boolean d_Day;
    private boolean autoTimeCheck;

    //엔티티로 바꾸는 메서드
    public Schedule ConvertToEntity(Member member) {
        return Schedule.builder()
                .member(member) // 누가 작성했는지, 연관관계 설정
                .scheduleName(this.scheduleName)
                .scheduleStartTime(this.scheduleStartTime)
                .scheduleEndTime(this.scheduleEndTime)
                .placeName(this.placeName)
                .placeAddress(this.PlaceAddress)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .memo(this.memo)
                .d_Day(this.d_Day)
                .notified(false)
                .autoTimeCheck(this.autoTimeCheck)
                .build();
    }


}
