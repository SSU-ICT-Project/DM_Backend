package com.dm.dmbackend.domain.goal.mainGoal.repository;

import com.dm.dmbackend.domain.goal.mainGoal.entity.MainGoal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MainGoalRepository extends JpaRepository<MainGoal, Long> {
    // 멤버가 작성한 모든 목표
    @Query("""
        SELECT m
        FROM MainGoal m
        WHERE m.member.id = :memberId
        ORDER BY m.deadline ASC NULLS LAST, m.createdAt ASC
    """)
    Page<MainGoal> findByMemberIdWithOrder(@Param("memberId") Long memberId, Pageable pageable);

}
