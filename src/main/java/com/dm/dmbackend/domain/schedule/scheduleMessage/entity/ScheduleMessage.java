package com.dm.dmbackend.domain.schedule.scheduleMessage.entity;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.schedule.schedule.entity.Schedule;
import com.dm.dmbackend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleMessage extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(length = 1000)
    private String message;

    private LocalDateTime scheduleTime;
}
