package com.dm.dmbackend.global.initData;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.dto.req.AdminForm;
import com.dm.dmbackend.domain.account.member.dto.req.MemberForm;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.repository.MemberRepository;
import com.dm.dmbackend.domain.account.member.service.MemberService;
import com.dm.dmbackend.domain.goal.mainGoal.entity.MainGoal;
import com.dm.dmbackend.domain.goal.mainGoal.repository.MainGoalRepository;
import com.dm.dmbackend.domain.goal.subGoal.entity.SubGoal;
import com.dm.dmbackend.domain.goal.subGoal.repository.SubGoalRepository;
import com.dm.dmbackend.global.exception.ReturnCode;
import com.dm.dmbackend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto.ConvertToLoginUserDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotProdService {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final MainGoalRepository mainGoalRepository;
    private final SubGoalRepository subGoalRepository;

    @Transactional
    public void initDummyData() {
        // 유저 1~5 + 관리자 1명 생성
        List<Member> members = createMembers();

        // 5명 모두 서로 팔로우하게 만들기
        createFollowRelations(members);

        // 목표/하위목표 더미 데이터 생성 (유저 1~5만 대상)
        createGoals(members);
    }

    // 유저 1, 2, 3, 4, 5 생성
    private List<Member> createMembers() {
        List<String> names = List.of("서울", "인천", "강릉", "부산", "제주");
        List<String> jobs = List.of("개발자", "의사", "파일럿", "변호사", "모델");
        List<String> nicknames = List.of("seoul_gangnam", "incheon_songdo", "gangneung_beach", "busan_haeundae", "jeju_seaside");
        Member.MotivationType[] motivationTypes = Member.MotivationType.values();
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            MemberForm memberForm = MemberForm.builder()
                    .nickname(nicknames.get(i))
                    .job(jobs.get(i))
                    .email("user" + (i + 1) + "@example.com")
                    .password("1234")
                    .motivationType(motivationTypes[i % motivationTypes.length])
                    .gender(i % 2 == 0 ? Member.Gender.MALE : Member.Gender.FEMALE)
                    .birthday(LocalDate.of(2001, i + 1, 1))
                    .build();
            memberService.signup(memberForm);

            Member member = memberRepository.findByEmail(memberForm.getEmail())
                    .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
            members.add(member);
        }

        // 관리자 생성
        AdminForm adminForm = AdminForm.builder()
                .nickname("admin6")
                .email("admin6@gmail.com")
                .job(jobs.get(0))
                .password("admin6")
                .motivationType(Member.MotivationType.VISION)
                .gender(Member.Gender.MALE)
                .memberRole(Member.MemberRole.ROLE_ADMIN)
                .birthday(LocalDate.of(2001,01,01))
                .build();
        memberService.adminSignup(adminForm);
        Member member = memberRepository.findByEmail(adminForm.getEmail())
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        members.add(member);

        return members;
    }

    // 5명 모두 서로 팔로우하게 만들기
    private void createFollowRelations(List<Member> members) {
        for (int i = 0; i < members.size(); i++) {
            Member fromMember = members.get(i);
            LoginUserDto fromLoginUser = ConvertToLoginUserDto(fromMember);
            for (int j = 0; j < members.size(); j++) {
                if (i == j) continue;
                Member toMember = members.get(j);
                memberService.followReq(toMember.getId(), fromLoginUser);
                LoginUserDto toLoginUser = ConvertToLoginUserDto(toMember);
                memberService.acceptFollowReq(fromMember.getId(), toLoginUser);
            }
        }
    }

    // 목표/하위목표 더미 데이터 생성 (유저 1~5만 대상)
    private void createGoals(List<Member> members) {
        // members = [유저1, 유저2, 유저3, 유저4, 유저5, 관리자]
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
                .deadline(deadline)   // LocalDate
                .checked(checked)
                .build();
        return mainGoalRepository.save(mg);
    }

    private void saveSubGoal(Member owner, MainGoal main, String content, LocalDate deadline, boolean checked) {
        SubGoal sg = SubGoal.builder()
                .member(owner)
                .mainGoal(main)
                .content(content)
                .deadline(deadline)   // null 허용
                .checked(checked)
                .build();
        subGoalRepository.save(sg);
    }
}
