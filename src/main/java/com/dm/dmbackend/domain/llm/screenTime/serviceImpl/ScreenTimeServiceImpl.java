package com.dm.dmbackend.domain.llm.screenTime.serviceImpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.goal.mainGoal.service.MainGoalService;
import com.dm.dmbackend.domain.llm.screenTime.dto.internal.UserContext;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeCureRequest;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeMotivateRequest;
import com.dm.dmbackend.domain.llm.screenTime.entity.ScreenTime;
import com.dm.dmbackend.domain.llm.screenTime.repository.ScreenTimeRepository;
import com.dm.dmbackend.domain.llm.screenTime.service.GoalDetail;
import com.dm.dmbackend.domain.llm.screenTime.service.ScreenTimeCure;
import com.dm.dmbackend.domain.llm.screenTime.service.ScreenTimeMotivate;
import com.dm.dmbackend.domain.llm.screenTime.service.ScreenTimeService;
import com.dm.dmbackend.domain.notification.entity.Notification;
import com.dm.dmbackend.global.common.utils.validator.NotificationValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreenTimeServiceImpl implements ScreenTimeService {
    private final ScreenTimeRepository screenTimeRepository;
    private final MainGoalService mainGoalService;
    private final ScreenTimeCure cure;
    private final ScreenTimeMotivate motivate;
    private final GoalDetail goalDetail;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // 중독 치료 메시지 생성
    @Override
    @Transactional
    public void getScreenTimeCure(ScreenTimeCureRequest screenTimeCureRequest,
                                                         LoginUserDto loginUser) {
        NotificationValidator.validateNotification(loginUser);
        String ragInput = toCureRagPayload(screenTimeCureRequest);
        UserContext ctx = buildUserContext(loginUser);
        // 자동 RAG 호출 (Retriever가 pgvector에서 문맥을 가져와 {{information}}에 자동 주입)
        String cureMessage = cure.message(
                ctx.getMotivationPrompt(),
                nvl(ragInput),
                nvl(ctx.getGoalSummary()),
                ctx.getUserData()
        );
        // DB 저장
        ScreenTime screenTime = ScreenTime.builder()
                .member(loginUser.ConvertToMember())
                .screenTimeData(ragInput)
                .message(cureMessage)
                .messageType(ScreenTime.MessageType.CURE)
                .build();
        screenTimeRepository.save(screenTime);
        // 중독 치료 메시지 이벤트 생성
        Notification notification = Notification.builder()
                .senderId(loginUser.getId())
                .senderNickname(loginUser.getNickname())
                .senderProfileUrl(loginUser.getProfileImageUrl())
                .receiverId(loginUser.getId())
                .objectId(screenTime.getId())
                .content(cureMessage)
                .targetObject(Notification.TargetObject.Cure)
                .build();
        try {
            String message = objectMapper.writeValueAsString(notification);
            kafkaTemplate.send("cure-topic", message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Notification: {}", e.getMessage());
        }
    }

    // 동기부여 메시지 생성
    @Override
    @Transactional
    public void getScreenTimeMotivate(ScreenTimeMotivateRequest screenTimeMotivateRequest,
                                      LoginUserDto loginUser){
        NotificationValidator.validateNotification(loginUser);
        String accessAppData = screenTimeMotivateRequest.getAccessAppData();
        UserContext ctx = buildUserContext(loginUser);
        // 목표 구체화 먼저 생성
        String goalDetailMsg;
        try {
            goalDetailMsg = goalDetail.message(
                    ctx.getMotivationPrompt(),
                    nvl(ctx.getGoalSummary()),
                    ctx.getUserData()
            );
        } catch (Exception e) {
            log.warn("GoalDetail LLM failed: {}", e.toString());
            goalDetailMsg = ""; // 안전한 폴백
        }
        // 동기부여 메시지 생성 (goal_detail 주입!)
        String motivateMessage = motivate.message(
                ctx.getMotivationPrompt(),
                nvl(accessAppData),
                nvl(ctx.getGoalSummary()),
                nvl(goalDetailMsg),
                ctx.getUserData()
        );
        // DB 저장
        ScreenTime screenTime = ScreenTime.builder()
                .member(loginUser.ConvertToMember())
                .accessAppData(accessAppData)
                .message(motivateMessage)
                .messageType(ScreenTime.MessageType.MOTIVATE)
                .build();
        screenTimeRepository.save(screenTime);
        // 동기부여 메시지 이벤트 생성
        Notification notification = Notification.builder()
                .senderId(loginUser.getId())
                .senderNickname(loginUser.getNickname())
                .senderProfileUrl(loginUser.getProfileImageUrl())
                .receiverId(loginUser.getId())
                .objectId(screenTime.getId())
                .content(motivateMessage)
                .targetObject(Notification.TargetObject.Motivate)
                .build();
        try {
            String message = objectMapper.writeValueAsString(notification);
            kafkaTemplate.send("motivate-topic", message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Notification: {}", e.getMessage());
        }
    }

    // ----------------- 헬퍼 메서드 -----------------

    private String nvl(Object v) { return v == null ? "" : String.valueOf(v); }

    // 동기부여 프롬프트 매핑
    private String mapMotivationPrompt(Member.MotivationType type) {
        Member.MotivationType t = (type == null) ? Member.MotivationType.ACTION : type;
        return switch (t) {
            case EMOTIONAL -> """
            감성 자극형:
            - 따뜻한 공감과 위로를 중심으로 메시지를 작성하세요.
            - 사용자의 감정을 이해하고 격려하는 톤을 사용하세요.
            """;
            case VISION -> """
            미래/비전 제시형:
            - 장기적인 목표와 긍정적인 미래를 강조하세요.
            - 사용자가 지금의 행동이 미래의 성취로 이어진다는 점을 부각하세요.
            """;
            case ACTION -> """
            구체적 행동 제시형:
            - 지금 바로 실천 가능한 구체적인 행동을 제안하세요.
            - 사용자가 즉시 따라할 수 있도록 명확한 지시를 포함하세요.
            """;
            case COMPETITION -> """
            비교/경쟁 자극형:
            - 다른 사람과의 비교나 경쟁심을 유발하는 메시지를 작성하세요.
            - 더 나은 성과를 향해 도전하도록 동기를 부여하세요.
            """;
        };
    }

    // 사용자 메타 + 목표 요약을 포함한 컨텍스트 데이터 생성
    private UserContext buildUserContext(LoginUserDto loginUser) {
        String motivationPrompt = mapMotivationPrompt(loginUser.getMotivationType());
        LocalDate birthday = loginUser.getBirthday();
        int age = birthday != null ? Period.between(birthday, LocalDate.now()).getYears() : 0;
        String userData = String.format("나이: %d, 성별: %s, 직업: %s",
                age, nvl(loginUser.getGender()), nvl(loginUser.getJob()));

        // RAG용 목표 요약 (상위 3개)
        String goalSummary = mainGoalService.buildCompactGoalSummary(loginUser, 3);
        return new UserContext(motivationPrompt, userData, goalSummary);
    }

    // 앱별 사용시간을 합산·정렬한 최소 JSON 생성
    private String toCureRagPayload(ScreenTimeCureRequest req) {
        List<ScreenTimeCureRequest.AppUsage> usages =
                Optional.ofNullable(req.getAppUsages()).orElseGet(Collections::emptyList);
        // appName 기준 합산(동일 앱 중복 보고 대비)
        Map<String, Integer> byApp = usages.stream()
                .filter(u -> u.getAppName() != null && !u.getAppName().isBlank())
                .collect(Collectors.toMap(
                        u -> u.getAppName().trim(),
                        ScreenTimeCureRequest.AppUsage::getUsageTimeMinutes,
                        Integer::sum,
                        LinkedHashMap::new
                ));
        // 사용시간 내림차순 정렬 후 리스트로 변환
        List<Map<String, Object>> apps = byApp.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .map(e -> Map.<String, Object>of(
                        "appName", e.getKey(),
                        "usageTimeMinutes", e.getValue()
                ))
                .collect(Collectors.toList());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("totalScreenTimeMinutes", req.getTotalScreenTime());
        payload.put("apps", apps);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build RAG payload", e);
        }
    }
}
