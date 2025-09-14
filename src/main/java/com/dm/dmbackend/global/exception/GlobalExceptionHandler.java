package com.dm.dmbackend.global.exception;

import com.dm.dmbackend.global.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외: ReturnCode 기준으로만 응답 (오버라이드 메시지 사용 안 함)
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceException(ServiceException ex) {
        var rc = ex.getReturnCode();
        // 참고: ex.getMessage()는 로그로만 사용 (바디로는 싣지 않음)
        log.warn("ServiceException: code={}, status={}, msg={}",
                rc.getCode(), rc.getStatus(), ex.getMessage());
        return ResponseEntity.status(rc.getStatus())
                .body(ApiResponse.failure(rc));
    }

    // 인증/인가: 스프링 시큐리티 표준 예외 매핑
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        log.warn("AuthenticationException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure(ReturnCode.UNAUTHORIZED));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("AccessDeniedException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(ReturnCode.FORBIDDEN));
    }

    // 바인딩/검증 오류 (@Valid) → 필드별 에러맵을 errorData로 내려줌
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(ReturnCode.BAD_REQUEST, errors));
    }

    // 파라미터 검증 (@Validated on @RequestParam, @PathVariable 등) → 경로/파라미터 이름을 key로
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
        log.warn("ConstraintViolation: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(ReturnCode.BAD_REQUEST, errors));
    }

    // 잘못된 본문/타입/누락 파라미터 → 간단한 reason만 errorData로 전달
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBadRequest(RuntimeException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        Map<String, String> detail = Map.of("reason", "요청 형식이 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(ReturnCode.BAD_REQUEST, detail));
    }

    // 지원하지 않는 HTTP 메서드 → 405 + reason
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not allowed: {}", ex.getMessage());
        Map<String, String> detail = Map.of("reason", "지원하지 않는 HTTP 메서드입니다.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.failure(ReturnCode.BAD_REQUEST, detail));
        // ReturnCode에 METHOD_NOT_ALLOWED(405) 추가하면 여기서 그 코드로 내려도 좋습니다.
    }

    // 경로 없음 (mvc: throw-exception-if-no-handler-found=true 설정 필요)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException ex) {
        log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ReturnCode.NOT_FOUND));
    }

    // 데이터 무결성/중복 등 → 간단한 힌트를 errorData로
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        Map<String, String> detail = Map.of("reason", "데이터 무결성 제약 조건 위반");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(ReturnCode.CONFLICT, detail));
    }

    // 최종 catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(ReturnCode.INTERNAL_SERVER_ERROR));
    }
}
