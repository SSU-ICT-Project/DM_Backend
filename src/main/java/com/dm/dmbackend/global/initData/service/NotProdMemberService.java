package com.dm.dmbackend.global.initData.service;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.dto.req.AdminForm;
import com.dm.dmbackend.domain.account.member.dto.req.MemberForm;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.entity.TimeRange;
import com.dm.dmbackend.domain.account.member.repository.MemberRepository;
import com.dm.dmbackend.domain.account.member.service.MemberService;
import com.dm.dmbackend.global.exception.ReturnCode;
import com.dm.dmbackend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto.ConvertToLoginUserDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotProdMemberService {
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    //유저 1~5 + 관리자 1명 생성 후 List<Member> 반환
    @Transactional
    public List<Member> createMembers() {
        List<String> names = List.of("서울", "인천", "강릉", "부산", "제주");
        List<String> jobs = List.of("개발자", "의사", "파일럿", "변호사", "모델");
        List<String> nicknames = List.of("seoul_gangnam", "incheon_songdo", "gangneung_beach", "busan_haeundae", "jeju_seaside");
        Member.MotivationType[] motivationTypes = Member.MotivationType.values();
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            int minutes = 10 + (i * 5); // 10,15,20,25,30
            MemberForm memberForm = MemberForm.builder()
                    .nickname(nicknames.get(i))
                    .job(jobs.get(i))
                    .email("user" + (i + 1) + "@example.com")
                    .password("1234")
                    .motivationType(motivationTypes[i % motivationTypes.length])
                    .gender(i % 2 == 0 ? Member.Gender.MALE : Member.Gender.FEMALE)
                    .birthday(LocalDate.of(2001, i + 1, 1))
                    .averagePreparationTime(LocalTime.of(0, minutes))
                    .devTimePerDay(buildDevTimePerDayShifted(i))   // 유저1: 월/화, 유저2: 화/수, ..., 유저5: 금/토
                    .distractionAppList(buildDistractionApps(i + 1)) // 1개 → 5개
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
                .birthday(LocalDate.of(2001, 1, 1))
                .build();
        memberService.adminSignup(adminForm);
        Member admin = memberRepository.findByEmail(adminForm.getEmail())
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        members.add(admin);

        return members;
    }

    // 5명 모두 서로 팔로우하게 만들기
    @Transactional
    public void createFollowRelations(List<Member> members) {
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

    // ----------------- 헬퍼 메서드 -----------------

    // 유저1: MONDAY(2슬롯), TUESDAY(1슬롯)
    // 유저2: TUESDAY, WEDNESDAY ... 유저5: FRIDAY, SATURDAY
    private Map<DayOfWeek, List<TimeRange>> buildDevTimePerDayShifted(int shiftDays) {
        Map<DayOfWeek, List<TimeRange>> map = new LinkedHashMap<>();
        DayOfWeek dayA = DayOfWeek.MONDAY.plus(shiftDays % 7);
        DayOfWeek dayB = dayA.plus(1);
        List<TimeRange> twoSlots = List.of(
                tr("06:00", "07:30"),
                tr("20:00", "22:00")
        );
        List<TimeRange> oneSlot = List.of(
                tr("20:00", "22:00")
        );
        map.put(dayA, twoSlots);
        map.put(dayB, oneSlot);
        return map;
    }

    // ["YouTube"] → ["YouTube","Instagram"] → ... → 최대 5개
    private List<String> buildDistractionApps(int count) {
        List<String> base = List.of("YouTube", "Instagram", "Facebook", "TikTok", "Thread");
        count = Math.max(1, Math.min(count, base.size()));
        return new ArrayList<>(base.subList(0, count));
    }

    // TimeRange 생성 유틸 (프로퍼티/생성자 다르면 여기를 맞춰 수정)
    private static TimeRange tr(String start, String end) {
        TimeRange r = new TimeRange();
        r.setStart(LocalTime.parse(start));
        r.setEnd(LocalTime.parse(end));
        return r;
    }
}
