package com.dm.DM_Backend.domain.account.member.repository;

import com.dm.DM_Backend.domain.account.member.entity.Member;
import com.dm.DM_Backend.domain.account.member.entity.MemberFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberFollowRepository extends JpaRepository<MemberFollow, Long> {
    // 팔로우 관계 존재여부 확인
    boolean existsByFollowAndFollowed(Member follow, Member followed);

    // 팔로우 관계 가져오기
    Optional<MemberFollow> findByFollowAndFollowed(Member follow, Member followed);

    // 팔로우중인 유저의 ID리스트 조회
    @Query("SELECT mf.followed.id FROM MemberFollow mf WHERE mf.follow.id = :followerId")
    List<Long> findFollowedMemberIdsByFollowerId(@Param("followerId") Long followerId);
}
