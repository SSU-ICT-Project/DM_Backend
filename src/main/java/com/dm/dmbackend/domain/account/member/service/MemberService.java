package com.dm.dmbackend.domain.account.member.service;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.dto.req.AdminSignUpRequest;
import com.dm.dmbackend.domain.account.member.dto.req.MemberSignUpRequest;
import com.dm.dmbackend.domain.account.member.dto.res.DetailMemberResponse;
import com.dm.dmbackend.domain.account.member.dto.res.MemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {
    // 회원가입 [가데이터/초기관리자 생성]
    void adminSignup(AdminSignUpRequest adminSignUpRequest);

    // 회원가입
    void signup(MemberSignUpRequest memberSignUpRequest);

    // 본인 회원정보 조회
    MemberResponse getMyInfo(LoginUserDto loginUser);

    // 본인 상세회원정보 조회
    DetailMemberResponse getMyDetailInfo(LoginUserDto loginUser);

    // 다른 멤버의 회원정보 조회
    MemberResponse getMemberInfo(Long memberId);

    // 다른 멤버의 상세회원정보 조회
    DetailMemberResponse getDetailMemberInfo(Long memberId);

    // 회원정보 수정
    void updateMember(MemberSignUpRequest memberSignUpRequest, LoginUserDto loginUser);

    // 회원탈퇴
    void deleteMember(LoginUserDto loginUser);

    // 회원 검색하기
    Page<MemberResponse> searchMemberInfo(Pageable pageable, String keyword);

    // 팔로우 요청하기
    void followReq(Long memberId, LoginUserDto loginUser);

    // 팔로우 요청 취소하기
    void cancelFollowReq(Long memberId, LoginUserDto loginUser);

    // 팔로우 요청 수락하기
    void acceptFollowReq(Long memberId, LoginUserDto loginUser);

    // 팔로우 요청 거절하기
    void refuseFollowReq(Long memberId, LoginUserDto loginUser);

    // 팔로우 취소하기
    void cancelFollow(Long memberId, LoginUserDto loginUser);

    // 팔로워 목록에서 해당 유저 삭제하기
    void removeFollowed(Long memberId, LoginUserDto loginUser);
}
