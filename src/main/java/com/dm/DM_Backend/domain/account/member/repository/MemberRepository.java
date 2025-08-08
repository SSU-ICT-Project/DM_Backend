package com.dm.DM_Backend.domain.account.member.repository;

import com.dm.DM_Backend.domain.account.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이메일로 회원 존재여부 확인
    boolean existsByEmail(String email);

    // 이메일로 회원 찾기
    @EntityGraph(value = "Member.withAllRelations", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Member> findByEmail(String email);

    // 회원 검색하기
    @Query("SELECT m FROM Member m WHERE m.name LIKE %:keyword% OR m.nickname LIKE %:keyword%")
    Page<Member> findByKeyword(Pageable pageable, @Param("keyword") String keyword);
}
