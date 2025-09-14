package com.dm.dmbackend.global.initData.service;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.dto.req.AdminSignUpRequest;
import com.dm.dmbackend.domain.account.member.dto.req.MemberSignUpRequest;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.repository.MemberRepository;
import com.dm.dmbackend.domain.account.member.service.MemberService;
import com.dm.dmbackend.global.common.vo.Location;
import com.dm.dmbackend.global.exception.ReturnCode;
import com.dm.dmbackend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
            Location location = buildLocation(names.get(i));
            MemberSignUpRequest memberSignUpRequest = MemberSignUpRequest.builder()
                    .nickname(nicknames.get(i))
                    .job(jobs.get(i))
                    .email("user" + (i + 1) + "@example.com")
                    .password("1234")
                    .motivationType(motivationTypes[i % motivationTypes.length])
                    .gender(i % 2 == 0 ? Member.Gender.MALE : Member.Gender.FEMALE)
                    .birthday(LocalDate.of(2001, i + 1, 1))
                    .averagePreparationTime(LocalTime.of(0, minutes))
                    .distractionAppList(buildDistractionApps(i + 1)) // 1개 → 5개
                    .location(location)
                    .build();
            memberService.signup(memberSignUpRequest);
            Member member = memberRepository.findByEmail(memberSignUpRequest.getEmail())
                    .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
            members.add(member);
        }

        // 관리자 생성
        AdminSignUpRequest adminSignUpRequest = AdminSignUpRequest.builder()
                .nickname("admin6")
                .email("admin6@gmail.com")
                .job(jobs.get(0))
                .password("admin6")
                .motivationType(Member.MotivationType.THRILL_SEEKER)
                .gender(Member.Gender.MALE)
                .memberRole(Member.MemberRole.ROLE_ADMIN)
                .birthday(LocalDate.of(2001, 1, 1))
                .build();
        memberService.adminSignup(adminSignUpRequest);
        Member admin = memberRepository.findByEmail(adminSignUpRequest.getEmail())
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

    // ["YouTube"] → ["YouTube","Instagram"] → ... → 최대 5개
    private List<String> buildDistractionApps(int count) {
        List<String> base = List.of("YouTube", "Instagram", "Facebook", "TikTok", "Thread");
        count = Math.max(1, Math.min(count, base.size()));
        return new ArrayList<>(base.subList(0, count));
    }

    // 지역별 Location 생성
    private Location buildLocation(String name) {
        switch (name) {
            case "서울":
                return Location.builder()
                        .placeName("강남역")
                        .placeAddress("서울특별시 강남구 강남대로 396")
                        .latitude("37.4979")
                        .longitude("127.0276")
                        .build();
            case "인천":
                return Location.builder()
                        .placeName("송도 센트럴파크")
                        .placeAddress("인천 연수구 컨벤시아대로 160")
                        .latitude("37.3925")
                        .longitude("126.6440")
                        .build();
            case "강릉":
                return Location.builder()
                        .placeName("경포해변")
                        .placeAddress("강원특별자치도 강릉시 강문동")
                        .latitude("37.8056")
                        .longitude("128.9072")
                        .build();
            case "부산":
                return Location.builder()
                        .placeName("해운대해수욕장")
                        .placeAddress("부산 해운대구 우동")
                        .latitude("35.1587")
                        .longitude("129.1604")
                        .build();
            case "제주":
                return Location.builder()
                        .placeName("성산일출봉")
                        .placeAddress("제주 서귀포시 성산읍 성산리")
                        .latitude("33.4580")
                        .longitude("126.9410")
                        .build();
            default:
                return Location.builder()
                        .placeName("기본 장소")
                        .placeAddress("대한민국")
                        .latitude("0.0")
                        .longitude("0.0")
                        .build();
        }
    }
}
