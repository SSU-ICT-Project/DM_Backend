package com.dm.DM_Backend.domain.account.auth.loginUser;

import com.dm.DM_Backend.domain.account.member.repository.MemberRepository;
import com.dm.DM_Backend.global.exception.ReturnCode;
import com.dm.DM_Backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(LoginUser.class) != null &&
                parameter.getParameterType().equals(LoginUserDto.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails securityUser) {
            String email = securityUser.getUsername(); // UserDetails의 getUsername()은 이메일을 반환

            return memberRepository.findByEmail(email)
                    .map(LoginUserDto::ConvertToLoginUserDto)
                    .orElseThrow(() -> new ServiceException(ReturnCode.USER_NOT_FOUND));
        }
        return null;
    }
}
