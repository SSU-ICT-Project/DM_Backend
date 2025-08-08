package com.dm.DM_Backend.domain.account.member.serviceImpl;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.account.auth.service.AuthService;
import com.dm.DM_Backend.domain.account.member.dto.req.MemberForm;
import com.dm.DM_Backend.domain.account.member.dto.res.DetailMemberDto;
import com.dm.DM_Backend.domain.account.member.dto.res.MemberDto;
import com.dm.DM_Backend.domain.account.member.dto.res.SimpleMember;
import com.dm.DM_Backend.domain.account.member.entity.Member;
import com.dm.DM_Backend.domain.account.member.entity.MemberFollow;
import com.dm.DM_Backend.domain.account.member.entity.MemberFollowReq;
import com.dm.DM_Backend.domain.account.member.entity.MemberPage;
import com.dm.DM_Backend.domain.account.member.repository.MemberFollowRepository;
import com.dm.DM_Backend.domain.account.member.repository.MemberFollowReqRepository;
import com.dm.DM_Backend.domain.account.member.repository.MemberRepository;
import com.dm.DM_Backend.domain.account.member.service.MemberService;
import com.dm.DM_Backend.domain.notification.entity.Notification;
import com.dm.DM_Backend.global.exception.ReturnCode;
import com.dm.DM_Backend.global.exception.ServiceException;
import com.dm.DM_Backend.global.s3.S3Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final MemberFollowReqRepository memberFollowReqRepository;
    private final MemberFollowRepository memberFollowRepository;
    private final S3Service s3Service;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // 회원가입
    @Override
    @Transactional
    public Member signup(MemberForm memberForm) {
        if (memberRepository.existsByEmail((memberForm.getEmail()))) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        // 비밀번호가 없으면 null로 처리하거나 다른 처리를 할 수 있습니다.
        String encodedPassword = memberForm.getPassword() != null ? passwordEncoder.encode(memberForm.getPassword()) : null;
        Member member = Member.builder()
                .name(memberForm.getName())
                .nickname(memberForm.getNickname())
                .phone(memberForm.getPhone())
                .email(memberForm.getEmail())
                .password(encodedPassword)  // 인코딩된 비밀번호 저장
                .gender(memberForm.getGender())
                .birthday(memberForm.getBirthday())
                .build();
        memberRepository.save(member);
        return member;
    }

    // 본인 회원정보 조회
    @Override
    @Transactional
    public MemberDto getMyInfo(LoginUserDto loginUser) {
        return loginUserConvertToMemberInfo(loginUser);
    }

    // 본인 상세회원정보 조회
    @Override
    @Transactional
    public DetailMemberDto getMyDetailInfo(LoginUserDto loginUser){
        return loginUserConvertToDetailMemberInfo(loginUser);
    }

    // 다른 멤버의 회원정보 조회
    @Override
    @Transactional
    public MemberDto getMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        return memberConvertToMemberInfo(member);
    }

    // 다른 멤버의 상세회원정보 조회
    @Override
    @Transactional
    public DetailMemberDto getDetailMemberInfo(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        return memberConvertToDetailMemberInfo(member);
    }

    // 회원정보 수정
    @Override
    @Transactional
    public void updateMember(MemberForm memberForm, MultipartFile imageFile, LoginUserDto loginUser) {
        /// 기존 이미지 삭제 후 입력 받은 이미지 S3에 저장
        String imageUrl = loginUser.getProfileImageUrl(); // 기본적으로 기존 이미지 URL을 사용
        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 이미지 없으면 바로 새로운 이미지 저장
            if (imageUrl != null && !imageUrl.isEmpty()) {
                s3Service.deleteFile(imageUrl);
            }
            try {
                imageUrl = s3Service.uploadFile(imageFile, "profile-image");
            } catch (IOException e) {
                throw new ServiceException(ReturnCode.INTERNAL_ERROR);
            }
        } else {
            // imageFile이 없으면 기존 이미지가 있다면 삭제한다
            if (imageUrl != null && !imageUrl.isEmpty()) {
                s3Service.deleteFile(imageUrl); // 기존 이미지 삭제
            }
            imageUrl = null;
        }
        if (memberForm.getName() != null) {
            loginUser.setName(memberForm.getName());
        }
        if (memberForm.getNickname() != null) {
            loginUser.setNickname(memberForm.getNickname());
        }
        if (memberForm.getPhone() != null) {
            loginUser.setPhone(memberForm.getPhone());
        }
        if (memberForm.getEmail() != null) {
            loginUser.setEmail(memberForm.getEmail());
        }
        if (memberForm.getPassword() != null) {
            loginUser.setPassword(BCrypt.hashpw(memberForm.getPassword(), BCrypt.gensalt()));
        }
        if (memberForm.getGender() != null) {
            loginUser.setGender(memberForm.getGender());
        }
        if (memberForm.getBirthday() != null) {
            loginUser.setBirthday(memberForm.getBirthday());
        }
        loginUser.setProfileImageUrl(imageUrl);
        // LoginUserDto를 Member 엔티티로 변환
        Member memberEntity = loginUser.ConvertToMember();
        memberRepository.save(memberEntity);
    }

    // 회원탈퇴
    @Override
    @Transactional
    public void deleteMember(LoginUserDto loginUser) {
        // refreshToken 삭제
        authService.logout(loginUser);
        // DB에서 회원 조회
        Member memberEntity = memberRepository.findById(loginUser.getId())
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));

        // 연관된 데이터 삭제

        memberRepository.delete(memberEntity);
    }

    // 회원 검색하기
    @Override
    @Transactional
    public Page<MemberDto> searchMemberInfo(Pageable pageable, String keyword){
        checkPageSize(pageable.getPageSize());
        Page<Member> members = memberRepository.findByKeyword(pageable, keyword);
        return members.map(this::memberConvertToMemberInfo);
    }

    // 팔로우 요청하기
    @Override
    @Transactional
    public void followReq(Long memberId, LoginUserDto loginUser){
        Member followReq = loginUser.ConvertToMember();
        Member followRec = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        // 기존 팔로우 여부 확인
        boolean already_follow = memberFollowRepository.existsByFollowAndFollowed(followReq, followRec);
        if (already_follow) {
            throw new ServiceException(ReturnCode.ALREADY_FOLLOW);
        }
        // 중복 요청 방지
        boolean already_requested = memberFollowReqRepository.existsByFollowReqAndFollowRec(followReq, followRec);
        if (already_requested) {
            throw new ServiceException(ReturnCode.ALREADY_REQUESTED);
        }
        MemberFollowReq memberFollowReq = MemberFollowReq.builder()
                .followReq(followReq)
                .followRec(followRec)
                .build();
        memberFollowReqRepository.save(memberFollowReq);
        // 팔로우 요청 이벤트 생성
        Notification notification = Notification.builder()
                .senderId(followReq.getId())
                .senderNickname(followReq.getNickname())
                .senderProfileUrl(followReq.getProfileImageUrl())
                .receiverId(followRec.getId())
                .objectId(memberFollowReq.getId())
                .content("님이 팔로우를 요청하였습니다.")
                .targetObject(Notification.TargetObject.Follow)
                .build();
        try {
            String message = objectMapper.writeValueAsString(notification);
            kafkaTemplate.send("follow-topic", message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Notification: {}", e.getMessage());
        }
    }

    // 팔로우 요청 취소하기
    @Override
    @Transactional
    public void cancelFollowReq(Long memberId, LoginUserDto loginUser){
        Member followReq = loginUser.ConvertToMember();
        Member followRec = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        MemberFollowReq memberFollowReq = memberFollowReqRepository.findByFollowReqAndFollowRec(followReq, followRec)
                .orElseThrow(() -> new ServiceException(ReturnCode.REQUEST_NOT_FOUND));
        memberFollowReqRepository.delete(memberFollowReq);
    }

    // 팔로우 요청 수락하기
    @Override
    @Transactional
    public void acceptFollowReq(Long memberId, LoginUserDto loginUser){
        Member requester = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        Member receiver = loginUser.ConvertToMember();
        MemberFollowReq followReq = memberFollowReqRepository.findByFollowReqAndFollowRec(requester, receiver)
                .orElseThrow(() -> new ServiceException(ReturnCode.REQUEST_NOT_FOUND));
        memberFollowReqRepository.delete(followReq);
        MemberFollow memberFollow = MemberFollow.builder()
                .follow(requester)
                .followed(receiver)
                .build();
        memberFollowRepository.save(memberFollow);
        // 팔로우 수락 이벤트 생성
        Notification notification = Notification.builder()
                .senderId(loginUser.getId())
                .senderNickname(loginUser.getNickname())
                .senderProfileUrl(loginUser.getProfileImageUrl())
                .receiverId(memberId)
                .objectId(memberFollow.getId())
                .content("님이 팔로우 요청을 수락하였습니다.")
                .targetObject(Notification.TargetObject.Follow)
                .build();
        try {
            String message = objectMapper.writeValueAsString(notification);
            kafkaTemplate.send("follow-topic", message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Notification: {}", e.getMessage());
        }
    }

    // 팔로우 요청 거절하기
    @Override
    @Transactional
    public void refuseFollowReq(Long memberId, LoginUserDto loginUser){
        Member requester = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        Member receiver = loginUser.ConvertToMember();
        MemberFollowReq memberFollowReq = memberFollowReqRepository.findByFollowReqAndFollowRec(requester, receiver)
                .orElseThrow(() -> new ServiceException(ReturnCode.REQUEST_NOT_FOUND));
        memberFollowReqRepository.delete(memberFollowReq);
    }

    // 팔로우 취소하기
    @Override
    @Transactional
    public void cancelFollow(Long memberId, LoginUserDto loginUser){
        Member follow = loginUser.ConvertToMember();
        Member followed = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        MemberFollow memberFollow = memberFollowRepository.findByFollowAndFollowed(follow, followed)
                .orElseThrow(() -> new ServiceException(ReturnCode.FOLLOW_NOT_FOUND));
        memberFollowRepository.delete(memberFollow);
    }

    // 팔로워 목록에서 해당 유저 삭제하기
    @Override
    @Transactional
    public void removeFollowed(Long memberId, LoginUserDto loginUser){
        Member follow = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        Member followed = loginUser.ConvertToMember();
        MemberFollow memberFollow = memberFollowRepository.findByFollowAndFollowed(follow, followed)
                .orElseThrow(() -> new ServiceException(ReturnCode.FOLLOWER_NOT_FOUND));
        memberFollowRepository.delete(memberFollow);
    }

    // 요청 페이지 수 제한
    private void checkPageSize(int pageSize) {
        int maxPageSize = MemberPage.getMaxPageSize();
        if (pageSize > maxPageSize) {
            throw new ServiceException(ReturnCode.PAGE_REQUEST_FAIL);
        }
    }

    // LoginUser를 MemberInfo로 변환
    private MemberDto loginUserConvertToMemberInfo(LoginUserDto loginUser) {
        return MemberDto.builder()
                .id(loginUser.getId())
                .name(loginUser.getName())
                .nickname(loginUser.getNickname())
                .profileImageUrl(loginUser.getProfileImageUrl())
                .followMemberCount(loginUser.getFollowList().stream().count())
                .followedMemberCount(loginUser.getFollowedList().stream().count())
                .build();
    }

    // Member를 MemberInfo로 변환
    private MemberDto memberConvertToMemberInfo(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .name(member.getName())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .followMemberCount(member.getFollowList().stream().count())
                .followedMemberCount(member.getFollowedList().stream().count())
                .build();
    }

    // LoginUser를 DetailMemberInfo로 변환
    private DetailMemberDto loginUserConvertToDetailMemberInfo(LoginUserDto loginUser) {
        return DetailMemberDto.builder()
                .id(loginUser.getId())
                .name(loginUser.getName())
                .nickname(loginUser.getNickname())
                .phone(loginUser.getPhone())
                .email(loginUser.getEmail())
                .gender(loginUser.getGender())
                .birthday(loginUser.getBirthday())
                .profileImageUrl(loginUser.getProfileImageUrl())
                .followMemberCount(loginUser.getFollowList().stream().count())
                .followedMemberCount(loginUser.getFollowedList().stream().count())
                // 팔로우 목록 변환
                .followList(loginUser.getFollowList().stream()
                        .map(MemberFollow -> SimpleMember.builder()
                                .userId(MemberFollow.getFollowed().getId())
                                .userName(MemberFollow.getFollowed().getName())
                                .userNickname(MemberFollow.getFollowed().getNickname())
                                .profileImageUrl(MemberFollow.getFollowed().getProfileImageUrl())
                                .build()
                        )
                        .collect(Collectors.toList())
                )
                // 팔로워 목록 변환
                .followedList(loginUser.getFollowedList().stream()
                        .map(MemberFollow -> SimpleMember.builder()
                                .userId(MemberFollow.getFollow().getId())
                                .userName(MemberFollow.getFollow().getName())
                                .userNickname(MemberFollow.getFollow().getNickname())
                                .profileImageUrl(MemberFollow.getFollow().getProfileImageUrl())
                                .build()
                        )
                        .collect(Collectors.toList())
                )
                // 팔로우 요청 목록 변환 (현재 사용자가 요청한 팔로우)
                .followReqList(loginUser.getFollowReqList().stream()
                        .map(MemberFollowReq -> SimpleMember.builder()
                                .userId(MemberFollowReq.getFollowRec().getId())
                                .userName(MemberFollowReq.getFollowRec().getName())
                                .userNickname(MemberFollowReq.getFollowRec().getNickname())
                                .profileImageUrl(MemberFollowReq.getFollowRec().getProfileImageUrl())
                                .build()
                        )
                        .collect(Collectors.toList())
                )
                // 팔로우 받은 목록 변환 (다른 사용자가 요청한 팔로우)
                .followRecList(loginUser.getFollowRecList().stream()
                        .map(MemberFollowReq -> SimpleMember.builder()
                                .userId(MemberFollowReq.getFollowReq().getId())
                                .userName(MemberFollowReq.getFollowReq().getName())
                                .userNickname(MemberFollowReq.getFollowReq().getNickname())
                                .profileImageUrl(MemberFollowReq.getFollowReq().getProfileImageUrl())
                                .build()
                        )
                        .collect(Collectors.toList())
                )
                .build();
    }

    // Member를 DetailMemberInfo로 변환
    private DetailMemberDto memberConvertToDetailMemberInfo(Member member) {
        return DetailMemberDto.builder()
                .id(member.getId())
                .name(member.getName())
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .email(member.getEmail())
                .gender(member.getGender())
                .birthday(member.getBirthday())
                .profileImageUrl(member.getProfileImageUrl())
                .followMemberCount(member.getFollowList().stream().count())
                .followedMemberCount(member.getFollowedList().stream().count())
                // 팔로우 목록 변환
                .followList(member.getFollowList().stream()
                        .map(MemberFollow -> SimpleMember.builder()
                                .userId(MemberFollow.getFollowed().getId())
                                .userName(MemberFollow.getFollowed().getName())
                                .userNickname(MemberFollow.getFollowed().getNickname())
                                .profileImageUrl(MemberFollow.getFollowed().getProfileImageUrl())
                                .build()
                        )
                        .collect(Collectors.toList())
                )
                // 팔로워 목록 변환
                .followedList(member.getFollowedList().stream()
                        .map(MemberFollow -> SimpleMember.builder()
                                .userId(MemberFollow.getFollow().getId())
                                .userName(MemberFollow.getFollow().getName())
                                .userNickname(MemberFollow.getFollow().getNickname())
                                .profileImageUrl(MemberFollow.getFollow().getProfileImageUrl())
                                .build()
                        )
                        .collect(Collectors.toList())
                )
                // 팔로우 요청 목록 변환 (현재 사용자가 요청한 팔로우)
                .followReqList(member.getFollowReqList().stream()
                        .map(MemberFollowReq -> SimpleMember.builder()
                                .userId(MemberFollowReq.getFollowRec().getId())
                                .userName(MemberFollowReq.getFollowRec().getName())
                                .userNickname(MemberFollowReq.getFollowRec().getNickname())
                                .profileImageUrl(MemberFollowReq.getFollowRec().getProfileImageUrl())
                                .build()
                        )
                        .collect(Collectors.toList())
                )
                // 팔로우 받은 목록 변환 (다른 사용자가 요청한 팔로우)
                .followRecList(member.getFollowRecList().stream()
                        .map(MemberFollowReq -> SimpleMember.builder()
                                .userId(MemberFollowReq.getFollowReq().getId())
                                .userName(MemberFollowReq.getFollowReq().getName())
                                .userNickname(MemberFollowReq.getFollowReq().getNickname())
                                .profileImageUrl(MemberFollowReq.getFollowReq().getProfileImageUrl())
                                .build()
                        )
                        .collect(Collectors.toList())
                )
                .build();
    }
}
