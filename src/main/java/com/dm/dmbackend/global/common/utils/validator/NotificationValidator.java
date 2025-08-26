package com.dm.dmbackend.global.common.utils.validator;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.global.exception.ReturnCode;
import com.dm.dmbackend.global.exception.ServiceException;

public class NotificationValidator {
    // 알림 허용 여부 검증
    public static void validateNotification(LoginUserDto loginUser) {
        if (Boolean.FALSE.equals(loginUser.getUseNotification())) {
            throw new ServiceException(ReturnCode.NOTIFICATION_DISABLED);
        }
    }
}
