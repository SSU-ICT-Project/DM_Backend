package com.dm.dmbackend.global.common.utils.validator;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.global.exception.ReturnCode;
import com.dm.dmbackend.global.exception.ServiceException;

public class RoleValidator {
    // ROLE_ADMIN 아닌 경우 예외 처리
    public static void validateAdmin(LoginUserDto loginUser) {
        if (loginUser.getRole() != Member.MemberRole.ROLE_ADMIN) {
            throw new ServiceException(ReturnCode.NOT_AUTHORIZED);
        }
    }
}
