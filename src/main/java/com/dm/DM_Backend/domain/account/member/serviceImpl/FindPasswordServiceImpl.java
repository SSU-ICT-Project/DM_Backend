package com.dm.DM_Backend.domain.account.member.serviceImpl;

import com.dm.DM_Backend.domain.account.member.entity.Member;
import com.dm.DM_Backend.domain.account.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FindPasswordServiceImpl {
    private final JavaMailSender javaMailSender;
    private final RedisVerificationCodeServiceImpl redisVerificationCodeService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private SecureRandom secureRandom = new SecureRandom();

    // 이메일 인증코드 전송(비밀번호 찾기)
    public boolean sendEmailVerificationCode(String email) {
        //1.존재하는 유저인지 확인
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isEmpty())
            return false;

        //2.인증코드 생성 및 캐싱
        int code = 100000 + secureRandom.nextInt(900000);
        redisVerificationCodeService.saveCodeByEmail(email, code);

        //3.메일 발송
        SimpleMailMessage verificationCodeMail = new SimpleMailMessage();
        verificationCodeMail.setTo(email);
        verificationCodeMail.setSubject("[Letzgo] 비밀번호 재설정을 위한 인증코드입니다.");
        verificationCodeMail.setText("비밀번호 재설정을 위한 인증코드는 " + code + " 입니다.\n만료기간(10분) 전에 입력해주세요.");
        javaMailSender.send(verificationCodeMail);
        return true;
    }

    // 이메일 인증코드 인증 (비밀번호 찾기)
    public String verifyEmailVerificationCode(String email, int verificationCode) {
        //인증코드 조회
        int storedCode = redisVerificationCodeService.getCodeByEmail(email);
        if(storedCode == verificationCode) {
            //인증코드 맞을시 비밀번호 재설정용 토큰리턴
            redisVerificationCodeService.deleteCodeByEmail(email);
            String token = redisVerificationCodeService.generateAndStoreResetToken(email);
            return token;
        }
        return "INVALID_CODE";
    }

    //비밀번호 재설정
    @Transactional
    public boolean resetPassword(String email, String token, String newPassword) {
        //토큰 일치확인
        if (redisVerificationCodeService.checkResetToken(email, token)) {
            Optional<Member> member = memberRepository.findByEmail(email);
            member.ifPresent(m->m.setPassword(passwordEncoder.encode(newPassword)));

            redisVerificationCodeService.deleteResetToken(email);
            return true;
        }
        return false;
    }
}
