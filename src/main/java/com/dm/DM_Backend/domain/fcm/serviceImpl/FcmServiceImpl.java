package com.dm.DM_Backend.domain.fcm.serviceImpl;

import com.dm.DM_Backend.domain.fcm.dto.FcmMessage;
import com.dm.DM_Backend.domain.fcm.service.FcmService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    @Override
    public void sendMessageTo(FcmMessage message) {
        try {
            Message firebaseMessage = Message.builder()
                    .setToken(message.getTargetToken())
                    .setNotification(
                            Notification.builder()
                                    .setTitle(message.getTitle())
                                    .setBody(message.getBody())
                                    .build()
                    )
                    .build();

            String response = FirebaseMessaging.getInstance().send(firebaseMessage);
            log.info("FCM Admin SDK response: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM push failed", e);
        }
    }
}
