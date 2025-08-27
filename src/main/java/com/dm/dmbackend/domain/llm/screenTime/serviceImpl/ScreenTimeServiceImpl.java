package com.dm.dmbackend.domain.llm.screenTime.serviceImpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.goal.mainGoal.service.MainGoalService;
import com.dm.dmbackend.domain.llm.screenTime.dto.internal.UserContext;
import com.dm.dmbackend.domain.llm.screenTime.dto.req.ScreenTimeCureRequest;
import com.dm.dmbackend.domain.llm.screenTime.entity.ScreenTime;
import com.dm.dmbackend.domain.llm.screenTime.repository.ScreenTimeRepository;
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
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
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
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // 중독 치료 메시지 생성
    @Override
    @Transactional
    public void getScreenTimeCure(ScreenTimeCureRequest screenTimeCureRequest,
                                                         LoginUserDto loginUser) {
        String ragInput = toCureRagPayload(screenTimeCureRequest);
        UserContext ctx = buildUserContext(loginUser);
        // 자동 RAG 호출 (Retriever가 pgvector에서 문맥을 가져와 {{information}}에 자동 주입)
        String cureMessage = cure.message(
                ctx.getMotivationPrompt(),
                nvl(ragInput),
                nvl(ctx.getGoalSummary()),
                ctx.getUserData()
        );
        cureMessage = cureMessage.replaceAll("\\s*\\n\\s*", " ");
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
        NotificationValidator.validateNotification(loginUser);
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
    public void getScreenTimeMotivate(LoginUserDto loginUser){
        NotificationValidator.validateNotification(loginUser);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        UserContext ctx = buildUserContext(loginUser);
        // 동기부여 메시지 생성 (goal_detail 주입!)
        String motivateMessage = motivate.message(
                ctx.getMotivationPrompt(),
                nvl(LocalTime.now().format(formatter)),
                nvl(ctx.getGoalSummary()),
                ctx.getUserData()
        );
        motivateMessage = motivateMessage.replaceAll("\\s*\\n\\s*", " ");
        // DB 저장
        ScreenTime screenTime = ScreenTime.builder()
                .member(loginUser.ConvertToMember())
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
        NotificationValidator.validateNotification(loginUser);
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
        Member.MotivationType t = (type == null) ? Member.MotivationType.HABITUAL_WATCHER : type;
        return switch (t) {
            case HABITUAL_WATCHER -> """
                습관적 시청형:
                치료 방향 -> 습관 깨기, 짧은 대체 행동
                예시 -> “지금 5분만 멈추면, 내일이 달라집니다.”
            """;
            case COMFORT_SEEKER -> """
                위로 추구형:
                치료 방향 -> 공감, 정서 회복, 작은 성취 경험
                예시 -> “피곤할 땐 쉬어도 돼요. 하지만 진짜 회복은 목표에 다가설 때 옵니다.”
            """;
            case THRILL_SEEKER -> """
                자극 추구형:
                치료 방향 -> 도전·경쟁심 자극, 단기 챌린지 제시
                예시 -> “쇼츠가 널 잡을까, 네가 이길까? 지금 선택해보세요.”
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
