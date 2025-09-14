package com.dm.dmbackend.domain.fcm.serviceImpl;

import com.dm.dmbackend.domain.fcm.dto.res.FcmMessageResponse;
import com.dm.dmbackend.domain.fcm.service.FcmService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    @Override
    public void sendMessageTo(FcmMessageResponse message) {
        try {
            Message firebaseMessage = Message.builder()
                    .setToken(message.getTargetToken())
                    .putData("title", message.getTitle())
                    .putData("body", message.getBody())
                    .build();

            String response = FirebaseMessaging.getInstance().send(firebaseMessage);
            log.info("FCM Admin SDK response: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM push failed", e);
        }
    }
}
