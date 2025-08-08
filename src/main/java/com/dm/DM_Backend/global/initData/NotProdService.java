package com.dm.DM_Backend.global.initData;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
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
        List<String> nicknames = List.of("seoul_gangnam", "incheon_songdo", "gangneung_beach", "busan_haeundae", "jeju_seaside");
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            MemberForm memberForm = MemberForm.builder()
                    .name(names.get(i))
                    .nickname(nicknames.get(i))
                    .phone("010-" + (i + 1) + (i + 1) + (i + 1) + (i + 1) + "-" + (i + 1) + (i + 1) + (i + 1) + (i + 1))
                    .email("user" + (i + 1) + "@example.com")
                    .password("1234")
                    .gender(i % 2 == 0 ? Member.Gender.MALE : Member.Gender.FEMALE)
                    .birthday(LocalDate.of(2001, i + 1, 1))
                    .build();
            memberService.signup(memberForm);

            Member member = memberRepository.findByEmail(memberForm.getEmail())
                    .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
            members.add(member);
        }

        //이메일인증 테스트용 유저6
        MemberForm memberForm = MemberForm.builder()
                .name("test6")
                .nickname("test6")
                .phone("010-1234-4321")
                .email("uichan0610@gmail.com")
                .password("1234")
                .gender(Member.Gender.MALE)
                .birthday(LocalDate.of(2001,01,01))
                .build();
        memberService.signup(memberForm);

        Member member = memberRepository.findByEmail(memberForm.getEmail())
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
