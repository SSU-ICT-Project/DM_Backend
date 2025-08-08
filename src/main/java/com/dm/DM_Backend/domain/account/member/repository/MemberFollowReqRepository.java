package com.dm.DM_Backend.domain.account.member.repository;

import com.dm.DM_Backend.domain.account.member.entity.Member;
import com.dm.DM_Backend.domain.account.member.entity.MemberFollowReq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberFollowReqRepository extends JpaRepository<MemberFollowReq, Long> {
    // 팔로우 요청 존재여부 확인
    boolean existsByFollowReqAndFollowRec(Member followReq, Member followRec);

    // 팔로우 요청 가져오기
    Optional<MemberFollowReq> findByFollowReqAndFollowRec(Member followReq, Member followRec);
}
