package com.dm.DM_Backend.global.exception;

import com.dm.DM_Backend.domain.account.auth.exception.UnauthorizedException;
import com.dm.DM_Backend.global.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceException(ServiceException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(ex.getReturnCode()));
    }

    // 예외 누락 방지를 위해 최종 catch-all 핸들러도 추천
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ReturnCode.INTERNAL_SERVER_ERROR));
    }
}
