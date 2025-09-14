package com.dm.dmbackend.domain.fcm.service;

import com.dm.dmbackend.domain.fcm.dto.res.FcmMessageResponse;

public interface FcmService {
    void sendMessageTo(FcmMessageResponse message);
}
