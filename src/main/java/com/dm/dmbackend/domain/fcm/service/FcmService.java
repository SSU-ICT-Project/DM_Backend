package com.dm.dmbackend.domain.fcm.service;

import com.dm.dmbackend.domain.fcm.dto.FcmMessage;

public interface FcmService {
    void sendMessageTo(FcmMessage message);
}
