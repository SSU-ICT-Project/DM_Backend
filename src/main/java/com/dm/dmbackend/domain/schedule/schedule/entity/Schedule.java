package com.dm.dmbackend.domain.schedule.schedule.entity;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.global.common.entity.BaseEntity;
import com.dm.dmbackend.global.common.vo.Location;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 20)
    private String scheduleName;

    private LocalDateTime scheduleStartTime;

    private LocalDateTime scheduleEndTime;

    @Embedded
    private Location location;

    @Column(length = 200)
    private String memo;

    private boolean d_Day;

    private boolean autoTimeCheck;

    @Builder.Default
    private boolean notified = false;
}
