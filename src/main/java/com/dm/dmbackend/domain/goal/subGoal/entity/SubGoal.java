package com.dm.dmbackend.domain.goal.subGoal.entity;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.goal.mainGoal.entity.MainGoal;
import com.dm.dmbackend.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SubGoal extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mainGoal_id", nullable = false)
    private MainGoal mainGoal;

    @Column(length = 500)
    private String content;

    private LocalDate deadline;

    private Boolean checked;
}
