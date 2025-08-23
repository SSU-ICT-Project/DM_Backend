package com.dm.dmbackend.domain.schedule.entity;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.global.jpa.BaseEntity;
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
public class Schedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 20)
    private String scheduleName;

    private LocalDateTime scheduleStartTime;

    private LocalDateTime scheduleEndTime;

    @Column(length = 50)
    private String location;

    @Column(length = 200)
    private String memo;

    private boolean d_Day;

    private boolean autoTimeCheck;

}
