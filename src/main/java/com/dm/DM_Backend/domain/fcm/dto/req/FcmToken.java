package com.dm.DM_Backend.domain.fcm.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FcmToken {
    @NotNull(message = "FCM Token은 필수입니다.")
    private String fcmToken;
}
