package com.dm.DM_Backend.domain.llm.ingest.controller;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUser;
import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.llm.ingest.service.IngestService;
import com.dm.DM_Backend.global.common.response.ApiResponse;
import com.dm.DM_Backend.global.exception.ReturnCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest-api/v1/ingest")
@RequiredArgsConstructor
@Tag(name = "Thesis", description = "논문 임베딩 API")
public class IngestController {
    private final IngestService ingestService;

    // 논문 임베딩
    @PostMapping
    @Operation(summary = "논문 임베딩")
    public ApiResponse<String> ingestThesis(@LoginUser LoginUserDto loginUser) {
        ingestService.ingestAll(loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 임베딩된 논문의 청크 개수 조회
    @GetMapping
    @Operation(summary = "임베딩된 논문의 청크 개수 조회")
    public ApiResponse<Long> getEmbeddingCount(@LoginUser LoginUserDto loginUser) {
        return ApiResponse.of(ingestService.getEmbeddingCount(loginUser));
    }

    // 임베딩된 논문 전체 삭제
    @DeleteMapping
    @Operation(summary = "임베딩된 논문 전체 삭제")
    public ApiResponse<String> clearEmbeddings(@LoginUser LoginUserDto loginUser) {
        ingestService.clearAll(loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }
}
