package com.dm.DM_Backend.domain.fcm.service;

import com.dm.DM_Backend.domain.fcm.dto.FcmMessage;

public interface FcmService {
    void sendMessageTo(FcmMessage message);
}
