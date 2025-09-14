package com.dm.dmbackend.domain.fcm.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@AllArgsConstructor
@SuperBuilder
public class FcmMessageResponse {
    private String targetToken;
    private String title;
    private String body;
}
