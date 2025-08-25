package com.dm.dmbackend.global.initData;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.global.initData.service.NotProdGoalService;
import com.dm.dmbackend.global.initData.service.NotProdMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotProdController {
    private final NotProdMemberService notProdMemberService;
    private final NotProdGoalService notProdGoalService;

    @Transactional
    public void initDummyData() {
        // 유저 1~5 + 관리자 생성
        List<Member> members = notProdMemberService.createMembers();

        // 팔로우 관계 생성
        notProdMemberService.createFollowRelations(members);

        // 목표/하위목표 더미 생성 (유저 1~5)
        notProdGoalService.createGoals(members);
    }
}
