package com.dm.dmbackend.global.initData.service;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.goal.mainGoal.entity.MainGoal;
import com.dm.dmbackend.domain.goal.mainGoal.repository.MainGoalRepository;
import com.dm.dmbackend.domain.goal.subGoal.entity.SubGoal;
import com.dm.dmbackend.domain.goal.subGoal.repository.SubGoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotProdGoalService {
    private final MainGoalRepository mainGoalRepository;
    private final SubGoalRepository subGoalRepository;

    // 상위목표/하위목표 더미 데이터 생성 (유저 5명만 대상)
    @Transactional
    public void createGoals(List<Member> members) {
        // members = [유저1, 유저2, 유저3, 유저4, 유저5, (관리자)]
        if (members.size() < 5) return;
        Member u1 = members.get(0);
        Member u2 = members.get(1);
        Member u3 = members.get(2);
        Member u4 = members.get(3);
        Member u5 = members.get(4);

        // ------------------ 1번 유저 ------------------
        MainGoal g1_1 = saveMainGoal(u1, "임원 미팅", toDate(3), false);
        saveSubGoal(u1, g1_1, "지난주 미팅 보고서 작성", toDate(1), false);

        MainGoal g1_2 = saveMainGoal(u1, "API명세서 작성", toDate(7), false);
        saveSubGoal(u1, g1_2, "API명세서 초안 리뷰", toDate(3), false);

        MainGoal g1_3 = saveMainGoal(u1, "토익 950점 달성", toDate(30), false);
        saveSubGoal(u1, g1_3, "영단어 100개 암기", null, false);
        saveSubGoal(u1, g1_3, "리스닝 3회차", null, false);
        saveSubGoal(u1, g1_3, "토익 모의시험 1회", toDate(20), false);

        // ------------------ 2번 유저 ------------------
        MainGoal g2_1 = saveMainGoal(u2, "팀 프로젝트 중간 발표", toDate(5), false);
        saveSubGoal(u2, g2_1, "발표 자료 최종 정리", toDate(1), false);
        saveSubGoal(u2, g2_1, "리허설 진행", toDate(2), false);

        MainGoal g2_2 = saveMainGoal(u2, "블로그 기술 글 작성", toDate(14), false);
        saveSubGoal(u2, g2_2, "초안 작성", null, false);
        saveSubGoal(u2, g2_2, "피드백 반영 및 수정", toDate(7), false);

        MainGoal g2_3 = saveMainGoal(u2, "마라톤 완주", toDate(60), false);
        saveSubGoal(u2, g2_3, "주 3회 러닝 (5km)", null, false);
        saveSubGoal(u2, g2_3, "하프코스 연습", toDate(30), false);

        // ------------------ 3번 유저 ------------------
        MainGoal g3_1 = saveMainGoal(u3, "포트폴리오 업데이트", toDate(10), false);
        saveSubGoal(u3, g3_1, "기존 프로젝트 리팩터링", null, false);
        saveSubGoal(u3, g3_1, "디자인 보강", toDate(3), false);
        saveSubGoal(u3, g3_1, "최종 업로드", toDate(7), false);

        MainGoal g3_2 = saveMainGoal(u3, "자격증 시험 합격", toDate(21), false);
        saveSubGoal(u3, g3_2, "기출문제 풀이", toDate(7), false);
        saveSubGoal(u3, g3_2, "모의고사 진행", toDate(14), false);

        MainGoal g3_3 = saveMainGoal(u3, "영어 면접 대비", toDate(45), false);
        saveSubGoal(u3, g3_3, "자기소개 스크립트 작성", null, false);
        saveSubGoal(u3, g3_3, "모의 인터뷰 1회차", toDate(30), false);

        // ------------------ 4번 유저 ------------------
        MainGoal g4_1 = saveMainGoal(u4, "밴드 공연", toDate(4), false);
        saveSubGoal(u4, g4_1, "연습실 리허설", toDate(1), false);
        saveSubGoal(u4, g4_1, "악기 점검", toDate(2), false);

        MainGoal g4_2 = saveMainGoal(u4, "유튜브 영상 업로드", toDate(15), false);
        saveSubGoal(u4, g4_2, "대본 작성", null, false);
        saveSubGoal(u4, g4_2, "촬영", toDate(5), false);
        saveSubGoal(u4, g4_2, "편집 및 자막 추가", toDate(10), false);

        MainGoal g4_3 = saveMainGoal(u4, "체력 단련", toDate(40), false);
        saveSubGoal(u4, g4_3, "주 4회 헬스장 운동", null, false);
        saveSubGoal(u4, g4_3, "인바디 체크", toDate(20), false);

        // ------------------ 5번 유저 ------------------
        MainGoal g5_1 = saveMainGoal(u5, "부모님 결혼기념일 준비", toDate(2), false);
        saveSubGoal(u5, g5_1, "꽃다발 주문", null, false);
        saveSubGoal(u5, g5_1, "케이크 예약", null, false);

        MainGoal g5_2 = saveMainGoal(u5, "집 인테리어 리뉴얼", toDate(12), false);
        saveSubGoal(u5, g5_2, "가구 배치 변경", toDate(7), false);
        saveSubGoal(u5, g5_2, "커튼 교체", null, false);

        MainGoal g5_3 = saveMainGoal(u5, "일본 여행 준비", toDate(25), false);
        saveSubGoal(u5, g5_3, "항공권 예약", null, false);
        saveSubGoal(u5, g5_3, "숙소 확정", null, false);
        saveSubGoal(u5, g5_3, "여행 일정표 작성", toDate(10), false);
    }

    // ----------------- 헬퍼 메서드 -----------------

    // D-<n> → 오늘 기준 +n일(LocalDate)
    private LocalDate toDate(int daysFromNow) {
        return LocalDate.now().plusDays(daysFromNow);
    }

    private MainGoal saveMainGoal(Member owner, String content, LocalDate deadline, boolean checked) {
        MainGoal mg = MainGoal.builder()
                .member(owner)
                .content(content)
                .deadline(deadline) // Entity가 LocalDateTime이면: .deadline(deadline.atStartOfDay())
                .checked(checked)
                .build();
        return mainGoalRepository.save(mg);
    }

    private void saveSubGoal(Member owner, MainGoal main, String content, LocalDate deadline, boolean checked) {
        SubGoal sg = SubGoal.builder()
                .member(owner)
                .mainGoal(main)
                .content(content)
                .deadline(deadline) // Entity가 LocalDateTime이면: deadline != null ? deadline.atStartOfDay() : null
                .checked(checked)
                .build();
        subGoalRepository.save(sg);
    }
}
