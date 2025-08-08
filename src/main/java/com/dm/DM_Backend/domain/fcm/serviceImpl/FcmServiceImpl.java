package com.dm.DM_Backend.domain.fcm.serviceImpl;

import com.dm.DM_Backend.domain.fcm.dto.FcmMessage;
import com.dm.DM_Backend.domain.fcm.service.FcmService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {
    private final ObjectMapper objectMapper;
    private static final OkHttpClient client = new OkHttpClient();

    @Value("${fcm.service-account-file}")
    private Resource serviceAccount;

    @Value("${fcm.project-id}")
    private String projectId;

    @Override
    public void sendMessageTo(FcmMessage message) {
        try {
            String accessToken = getAccessToken();
            String json = objectMapper.writeValueAsString(createFcmRequestBody(message));
            Request request = new Request.Builder()
                    .url("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send")
                    .post(RequestBody.create(json, MediaType.get("application/json; charset=utf-8")))
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json; UTF-8")
                    .build();
            try (Response response = client.newCall(request).execute()) {
                log.info("FCM v1 response: {}", response.body().string());
            }
        } catch (Exception e) {
            log.error("FCM push failed", e);
        }
    }

    // JWT 토큰 발급
    private String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(serviceAccount.getInputStream())
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    // HTTP v1 API용 Body 생성
    private Map<String, Object> createFcmRequestBody(FcmMessage message) {
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("token", message.getTargetToken());

        Map<String, String> notification = new HashMap<>();
        notification.put("title", message.getTitle());
        notification.put("body", message.getBody());
        messageMap.put("notification", notification);
        body.put("message", messageMap);
        return body;
    }
}
