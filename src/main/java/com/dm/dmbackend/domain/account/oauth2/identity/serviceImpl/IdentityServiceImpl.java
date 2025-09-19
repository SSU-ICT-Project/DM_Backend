package com.dm.dmbackend.domain.account.oauth2.identity.serviceImpl;

import com.dm.dmbackend.domain.account.member.dto.req.MemberRequest;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.entity.SocialAccount;
import com.dm.dmbackend.domain.account.member.repository.MemberRepository;
import com.dm.dmbackend.domain.account.member.repository.SocialAccountRepository;
import com.dm.dmbackend.domain.account.member.service.MemberService;
import com.dm.dmbackend.global.common.enums.OAuth2Provider;
import com.dm.dmbackend.domain.account.oauth2.identity.service.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityServiceImpl implements IdentityService {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;

    // (provider, socialId) 기준으로 멤버 보장
    @Transactional
    public Member ensureMemberBySocial(OAuth2Provider provider, String socialId, MemberRequest candidate) {
        return socialAccountRepository.findByProviderAndProviderUserId(provider, socialId)
                .map(SocialAccount::getMember)
                .orElseGet(() -> {
                    // 0) 이메일 없으면 명시적 실패 (혹은 여기서 강제 placeholder 생성)
                    if (candidate.getEmail() == null || candidate.getEmail().isBlank()) {
                        throw new IllegalStateException("소셜 회원가입에 email이 비어있습니다. placeholder 또는 signup 반환값 도입이 필요합니다.");
                    }
                    // 1) 이메일로 조회
                    Member member = memberRepository.findByEmail(candidate.getEmail()).orElse(null);
                    // 2) 없으면 가입 → 다시 조회
                    if (member == null) {
                        try {
                            memberService.signup(candidate); // void
                        } catch (DataIntegrityViolationException e) {
                            // 경쟁 보정
                            member = memberRepository.findByEmail(candidate.getEmail()).orElse(null);
                            if (member == null) throw e;
                        }
                        if (member == null) {
                            member = memberRepository.findByEmail(candidate.getEmail())
                                    .orElseThrow(() -> new IllegalStateException("회원 생성 확인 실패"));
                        }
                    }
                    // 3) 링크 저장
                    try {
                        socialAccountRepository.save(
                                SocialAccount.builder()
                                        .member(member)
                                        .provider(provider)
                                        .providerUserId(socialId)
                                        .providerEmail(candidate.getEmail())
                                        .displayName(candidate.getNickname())
                                        .build()
                        );
                    } catch (DataIntegrityViolationException e) {
                        return socialAccountRepository.findByProviderAndProviderUserId(provider, socialId)
                                .map(SocialAccount::getMember)
                                .orElseThrow(() -> e);
                    }
                    return member;
                });
    }
}
