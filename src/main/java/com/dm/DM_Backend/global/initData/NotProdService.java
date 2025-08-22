package com.dm.DM_Backend.global.initData;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.account.member.dto.req.AdminForm;
import com.dm.DM_Backend.domain.account.member.dto.req.MemberForm;
import com.dm.DM_Backend.domain.account.member.entity.Member;
import com.dm.DM_Backend.domain.account.member.repository.MemberRepository;
import com.dm.DM_Backend.domain.account.member.service.MemberService;
import com.dm.DM_Backend.global.exception.ReturnCode;
import com.dm.DM_Backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto.ConvertToLoginUserDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotProdService {
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Transactional
    public void initDummyData() {
        // 유저 1, 2, 3, 4, 5 생성
        List<Member> members = createMembers();

        // 5명 모두 서로 팔로우하게 만들기
        createFollowRelations(members);
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
}
