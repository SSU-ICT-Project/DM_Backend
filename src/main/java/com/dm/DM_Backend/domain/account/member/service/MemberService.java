package com.dm.DM_Backend.domain.account.member.service;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.account.member.dto.req.MemberForm;
import com.dm.DM_Backend.domain.account.member.dto.res.DetailMemberDto;
import com.dm.DM_Backend.domain.account.member.dto.res.MemberDto;
import com.dm.DM_Backend.domain.account.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {
    // 회원가입
    Member signup(MemberForm memberForm);

    // 본인 회원정보 조회
    MemberDto getMyInfo(LoginUserDto loginUser);

    // 본인 상세회원정보 조회
    DetailMemberDto getMyDetailInfo(LoginUserDto loginUser);

    // 다른 멤버의 회원정보 조회
    MemberDto getMemberInfo(Long memberId);

    // 다른 멤버의 상세회원정보 조회
    DetailMemberDto getDetailMemberInfo(Long memberId);

    // 회원정보 수정
    void updateMember(MemberForm memberForm, MultipartFile imageFile, LoginUserDto loginUser);

    // 회원탈퇴
    void deleteMember(LoginUserDto loginUser);

    // 회원 검색하기
    Page<MemberDto> searchMemberInfo(Pageable pageable, String keyword);

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
