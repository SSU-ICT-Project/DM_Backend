package com.dm.dmbackend.global.common.response;

import com.dm.dmbackend.global.exception.ReturnCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "success", "code", "message", "data", "page", "timestamp", "traceId" })
public class ApiResponse<T> {
    // 비즈니스 성공 여부
    private final boolean success;

    // 비즈니스 코드 (예: AUTH_001, USER_002 ...)
    private final String code;

    // 사용자 메시지 (i18n 대상이면 메시지키/로케일 매핑으로 대체 가능)
    private final String message;

    // 실데이터 (단일 객체 또는 리스트 모두 가능)
    private final T data;

    // 페이지 메타 정보 (페이지가 아닌 응답이면 null)
    private final PageMeta page;

    // 응답 생성 시각
    private final Instant timestamp;

    // 선택: 추적용 ID (MDC 등에서 꺼내 세팅)
    private final String traceId;

    // ----------------- Factory Methods -----------------

    // 데이터 없이 성공만 알리는 응답
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .success(true)
                .code(ReturnCode.SUCCESS.getCode())
                .message(ReturnCode.SUCCESS.getMessage())
                .timestamp(Instant.now())
                .build();
    }

    // 단건 데이터<T>를 받는 성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(ReturnCode.SUCCESS.getCode())
                .message(ReturnCode.SUCCESS.getMessage())
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    // PageResponse<T>를 받는 성공 응답(캐싱 최적화)
    public static <T> ApiResponse<List<T>> success(PageResponse<T> pageResponse) {
        return ApiResponse.<List<T>>builder()
                .success(true)
                .code(ReturnCode.SUCCESS.getCode())
                .message(ReturnCode.SUCCESS.getMessage())
                .data(pageResponse.getContent())
                .page(pageResponse.getPageMeta())
                .timestamp(Instant.now())
                .build();
    }

    // 실패 응답
    public static <T> ApiResponse<T> failure(ReturnCode rc) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(rc.getCode())
                .message(rc.getMessage())
                .timestamp(Instant.now())
                .build();
    }

    // 실패 응답(payload 포함)
    public static <T> ApiResponse<T> failure(ReturnCode rc, T errorData) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(rc.getCode())
                .message(rc.getMessage())
                .data(errorData)
                .timestamp(Instant.now())
                .build();
    }
}
