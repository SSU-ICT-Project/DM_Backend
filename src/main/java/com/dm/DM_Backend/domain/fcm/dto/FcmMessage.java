package com.dm.DM_Backend.domain.fcm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@AllArgsConstructor
@SuperBuilder
public class FcmMessage {
    private String targetToken;
    private String title;
    private String body;
}
