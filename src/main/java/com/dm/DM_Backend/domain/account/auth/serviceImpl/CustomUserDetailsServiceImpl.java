package com.dm.DM_Backend.domain.account.auth.serviceImpl;

import com.dm.DM_Backend.domain.account.member.entity.Member;
import com.dm.DM_Backend.domain.account.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        return org.springframework.security.core.userdetails.User.builder()
                .username(member.getEmail())
                .password(member.getPassword()) // 패스워드는 보통 사용 안 함 (JWT 기반 인증)
                .authorities("ROLE_USER") // 권한 설정 (추후 DB에서 가져올 수 있음)
                .build();
    }
}
