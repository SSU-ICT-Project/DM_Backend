package com.dm.dmbackend.domain.llm.digitalDetox.serviceImpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.goal.mainGoal.service.MainGoalService;
import com.dm.dmbackend.domain.llm.digitalDetox.dto.internal.UserContextDto;
import com.dm.dmbackend.domain.llm.digitalDetox.dto.req.DigitalDetoxCureRequest;
import com.dm.dmbackend.domain.llm.digitalDetox.entity.DigitalDetox;
import com.dm.dmbackend.domain.llm.digitalDetox.repository.DigitalDetoxRepository;
import com.dm.dmbackend.domain.llm.digitalDetox.service.DigitalDetoxCure;
import com.dm.dmbackend.domain.llm.digitalDetox.service.DigitalDetoxMotivate;
import com.dm.dmbackend.domain.llm.digitalDetox.service.DigitalDetoxService;
import com.dm.dmbackend.domain.notification.dto.NotificationPayload;
import com.dm.dmbackend.domain.notification.factory.NotificationPayloadFactory;
import com.dm.dmbackend.global.common.utils.validator.NotificationValidator;
import com.dm.dmbackend.global.kafka.event.notification.NotificationEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.dm.dmbackend.global.constants.KafkaKey.CURE_NOTIFICATION_TOPIC;
import static com.dm.dmbackend.global.constants.KafkaKey.MOTIVATE_NOTIFICATION_TOPIC;

@Service
@RequiredArgsConstructor
public class DigitalDetoxServiceImpl implements DigitalDetoxService {
    private final DigitalDetoxRepository digitalDetoxRepository;
    private final MainGoalService mainGoalService;
    private final DigitalDetoxCure cure;
    private final DigitalDetoxMotivate motivate;
    private final NotificationEventPublisher notificationEventPublisher;
    private final ObjectMapper objectMapper;

    // 중독 치료 메시지 생성
    @Override
    @Transactional
    public void getDigitalDetoxCure(DigitalDetoxCureRequest digitalDetoxCureRequest,
                                    LoginUserDto loginUser) {
        String ragInput = toCureRagPayload(digitalDetoxCureRequest);
        Member member = loginUser.ConvertToMember();
        UserContextDto ctx = buildUserContext(loginUser);
        String ragQuery = buildRagQuery(loginUser.getMotivationType()); // 검색 전용 짧은 질의
        // 자동 RAG 호출 (Retriever가 pgvector에서 문맥을 가져와 {{information}}에 자동 주입)
        String cureMessage = cure.message(
                nvl(ragInput),
                nvl(ctx.getGoalSummary()),
                ctx.getUserData(),
                ragQuery
        );
        cureMessage = cureMessage.replaceAll("\\s*\\n\\s*", " ");
        // DB 저장
        DigitalDetox digitalDetox = DigitalDetox.builder()
                .member(member)
                .screenTimeData(ragInput)
                .message(cureMessage)
                .messageType(DigitalDetox.MessageType.CURE)
                .build();
        digitalDetoxRepository.save(digitalDetox);
        // 중독 치료 메시지 알림 생성
        NotificationPayload payload = NotificationPayloadFactory.digitalDetoxCure(member, digitalDetox);
        NotificationValidator.validateNotification(loginUser);
        // 커밋 이후에만 발행 (롤백 시 이벤트 발행 방지)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationEventPublisher.publishNotification(payload, CURE_NOTIFICATION_TOPIC);
            }
        });
    }

    // 동기부여 메시지 생성
    @Override
    @Transactional
    public void getDigitalDetoxMotivate(LoginUserDto loginUser) {
        NotificationValidator.validateNotification(loginUser);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        Member member = loginUser.ConvertToMember();
        UserContextDto ctx = buildUserContext(loginUser);
        // 동기부여 메시지 생성 (goal_detail 주입!)
        String motivateMessage = motivate.message(
                ctx.getMotivationPrompt(),
                nvl(LocalTime.now().format(formatter)),
                nvl(ctx.getGoalSummary()),
                ctx.getUserData()
        );
        motivateMessage = motivateMessage.replaceAll("\\s*\\n\\s*", " ");
        // DB 저장
        DigitalDetox digitalDetox = DigitalDetox.builder()
                .member(member)
                .message(motivateMessage)
                .messageType(DigitalDetox.MessageType.MOTIVATE)
                .build();
        digitalDetoxRepository.save(digitalDetox);
        // 동기부여 메시지 알림 생성
        NotificationPayload payload = NotificationPayloadFactory.digitalDetoxMotivate(member, digitalDetox);
        NotificationValidator.validateNotification(loginUser);
        // 커밋 이후에만 발행 (롤백 시 이벤트 발행 방지)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationEventPublisher.publishNotification(payload, MOTIVATE_NOTIFICATION_TOPIC);
            }
        });
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
    private UserContextDto buildUserContext(LoginUserDto loginUser) {
        String motivationPrompt = mapMotivationPrompt(loginUser.getMotivationType());
        LocalDate birthday = loginUser.getBirthday();
        int age = birthday != null ? Period.between(birthday, LocalDate.now()).getYears() : 0;
        String userData = String.format("나이: %d, 성별: %s, 직업: %s",
                age, nvl(loginUser.getGender()), nvl(loginUser.getJob()));

        // 프롬프트용 목표 요약 (상위 3개)
        String goalSummary = mainGoalService.buildCompactGoalSummary(loginUser, 3);
        return new UserContextDto(motivationPrompt, userData, goalSummary);
    }

    // 앱별 사용시간을 합산·정렬한 최소 JSON 생성
    private String toCureRagPayload(DigitalDetoxCureRequest req) {
        List<DigitalDetoxCureRequest.AppUsage> usages =
                Optional.ofNullable(req.getAppUsages()).orElseGet(Collections::emptyList);
        // appName 기준 합산(동일 앱 중복 보고 대비)
        Map<String, Integer> byApp = usages.stream()
                .filter(u -> u.getAppName() != null && !u.getAppName().isBlank())
                .collect(Collectors.toMap(
                        u -> u.getAppName().trim(),
                        DigitalDetoxCureRequest.AppUsage::getUsageTimeMinutes,
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

    public String buildRagQuery(Member.MotivationType type) {
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        // 1) 공통 도메인
        terms.add("디지털 중독 치료");
        // 2) 성향에 따라 우선 문서축을 선택적으로 강화
        switch (type) {
            case HABITUAL_WATCHER -> {  // 습관적 시청형
                terms.addAll(List.of("스마트폰 중독 예방", "인지행동 집단치료"));
                terms.addAll(List.of("자기점검", "점진적 사용 감소", "습관 교체"));
            }
            case COMFORT_SEEKER -> {    // 위로 추구형
                terms.addAll(List.of("인지행동치료", "인지행동 음악치료", "중독음악치료"));
                terms.addAll(List.of("정서 조절", "스트레스", "음악치료"));
            }
            case THRILL_SEEKER -> {     // 자극 추구형
                terms.addAll(List.of("디지털 치료제", "saMD", "의료 목적의 소프트웨어"));
                terms.addAll(List.of("스마트폰 중독 예방", "인지행동 집단치료"));
                terms.addAll(List.of("대체 활동", "동기 강화 상담"));
            }
        }
        // 4) 너무 길어지면 상위 8~10개만 유지 (순서 보존)
        return terms.stream()
                .limit(10)
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
