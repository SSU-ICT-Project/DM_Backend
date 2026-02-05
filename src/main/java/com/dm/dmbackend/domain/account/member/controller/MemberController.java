package com.dm.dmbackend.domain.account.member.controller;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUser;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.dto.req.MemberRequest;
import com.dm.dmbackend.domain.account.member.dto.res.DetailMemberResponse;
import com.dm.dmbackend.domain.account.member.dto.res.MemberResponse;
import com.dm.dmbackend.domain.account.member.entity.MemberPage;
import com.dm.dmbackend.domain.account.member.service.MemberService;
import com.dm.dmbackend.global.common.response.ApiResponse;
import com.dm.dmbackend.global.security.TokenResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest-api/v1/member")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 API")
public class MemberController {
    private final MemberService memberService;
    private final TokenResolver tokenResolver;

    // 회원가입
    @PostMapping
    @Operation(summary = "회원가입")
    public ApiResponse<Void> signup(@RequestBody @Valid MemberRequest memberRequest) {
        memberService.signup(memberRequest);
        return ApiResponse.success();
    }

    // 본인 회원정보 조회
    @GetMapping
    @Operation(summary = "본인 회원정보 조회")
    public ApiResponse<MemberResponse> getMyInfo(@LoginUser LoginUserDto loginUser) {
        return ApiResponse.success(memberService.getMyInfo(loginUser));
    }

    // 본인 상세회원정보 조회
    @GetMapping("/detail")
    @Operation(summary = "본인 상세회원정보 조회")
    public ApiResponse<DetailMemberResponse> getMyDetailInfo(@LoginUser LoginUserDto loginUser) {
        return ApiResponse.success(memberService.getMyDetailInfo(loginUser));
    }

    // 다른 멤버의 회원정보 조회
    @GetMapping("/{memberId}")
    @Operation(summary = "다른 멤버의 회원정보 조회")
    public ApiResponse<MemberResponse> getMemberInfo(@PathVariable("memberId") Long memberId) {
        return ApiResponse.success(memberService.getMemberInfo(memberId));
    }

    // 다른 멤버의 상세회원정보 조회
    @GetMapping("/detail/{memberId}")
    @Operation(summary = "다른 멤버의 상세회원정보 조회")
    public ApiResponse<DetailMemberResponse> getDetailMemberInfo(@PathVariable("memberId") Long memberId) {
        return ApiResponse.success(memberService.getDetailMemberInfo(memberId));
    }

    // 회원정보 수정
    @PutMapping
    @Operation(summary = "회원정보 수정")
    public ApiResponse<Void> updateMemberInfo(@RequestBody @Valid MemberRequest memberRequest,
                                              @LoginUser LoginUserDto loginUser) {
        memberService.updateMember(memberRequest, loginUser);
        return ApiResponse.success();
    }

    // 회원탈퇴
    @DeleteMapping
    @Operation(summary = "회원탈퇴")
    public ApiResponse<Void> deleteMember(HttpServletRequest request, @LoginUser LoginUserDto loginUser) {
        String accessToken = tokenResolver.resolveAccess(request);
        memberService.deleteMember(loginUser, accessToken);
        return ApiResponse.success();
    }

    // 회원 검색하기
    @GetMapping("/search")
    @Operation(summary = "회원 검색하기")
    public ApiResponse<List<MemberResponse>> searchMemberInfo(@ModelAttribute MemberPage memberPage,
                                                              @RequestParam("keyword") String keyword) {
        Pageable pageable = PageRequest.of(memberPage.getPage(), memberPage.getSize());
        return ApiResponse.success(memberService.searchMemberInfo(pageable, keyword));
    }

    // 팔로우 요청하기
    @PostMapping("/follow/{memberId}")
    @Operation(summary = "팔로우 요청하기")
    public ApiResponse<Void> followReq(@PathVariable("memberId") Long memberId,
                                       @LoginUser LoginUserDto loginUser) {
        memberService.followReq(memberId, loginUser);
        return ApiResponse.success();
    }

    // 팔로우 요청 취소하기
    @DeleteMapping("/follow/{memberId}")
    @Operation(summary = "팔로우 요청 취소하기")
    public ApiResponse<Void> cancelFollowReq(@PathVariable("memberId") Long memberId,
                                             @LoginUser LoginUserDto loginUser) {
        memberService.cancelFollowReq(memberId, loginUser);
        return ApiResponse.success();
    }

    // 팔로우 요청 수락하기
    @PostMapping("/followReq/{memberId}")
    @Operation(summary = "팔로우 요청 수락하기")
    public ApiResponse<Void> acceptFollowReq(@PathVariable("memberId") Long memberId,
                                             @LoginUser LoginUserDto loginUser) {
        memberService.acceptFollowReq(memberId, loginUser);
        return ApiResponse.success();
    }

    // 팔로우 요청 거절하기
    @DeleteMapping("/followReq/{memberId}")
    @Operation(summary = "팔로우 요청 거절하기")
    public ApiResponse<Void> refuseFollowReq(@PathVariable("memberId") Long memberId,
                                             @LoginUser LoginUserDto loginUser) {
        memberService.refuseFollowReq(memberId, loginUser);
        return ApiResponse.success();
    }

    // 팔로우 취소하기
    @DeleteMapping("/followMember/{memberId}")
    @Operation(summary = "팔로우 취소하기")
    public ApiResponse<Void> cancelFollow(@PathVariable("memberId") Long memberId,
                                          @LoginUser LoginUserDto loginUser) {
        memberService.cancelFollow(memberId, loginUser);
        return ApiResponse.success();
    }

    // 팔로워 목록에서 해당 유저 삭제하기
    @DeleteMapping("/followed/{memberId}")
    @Operation(summary = "팔로워 목록에서 해당 유저 삭제하기")
    public ApiResponse<Void> removeFollowed(@PathVariable("memberId") Long memberId,
                                            @LoginUser LoginUserDto loginUser) {
        memberService.removeFollowed(memberId, loginUser);
        return ApiResponse.success();
    }
}
