package com.dm.dmbackend.domain.llm.thesis.controller;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUser;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.llm.thesis.dto.req.ThesisDeleteRequest;
import com.dm.dmbackend.domain.llm.thesis.dto.res.ThesisResponse;
import com.dm.dmbackend.domain.llm.thesis.entity.ThesisPage;
import com.dm.dmbackend.domain.llm.thesis.service.ThesisService;
import com.dm.dmbackend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/rest-api/v1/thesis")
@RequiredArgsConstructor
@Tag(name = "Thesis", description = "논문 관리 API")
public class ApiV1ThesisController {
    private final ThesisService thesisService;

    // 논문 업로드
    @PostMapping
    @Operation(summary = "논문 업로드")
    public ApiResponse<Void> uploadThesis(@RequestPart(value = "pdfFile") List<MultipartFile> pdfFiles,
                                          @LoginUser LoginUserDto loginUser) {
        thesisService.uploadThesis(pdfFiles, loginUser);
        return ApiResponse.success();
    }

    // 논문 조회
    @GetMapping
    @Operation(summary = "논문 조회")
    public ApiResponse<List<ThesisResponse>> getThesis(@ModelAttribute ThesisPage thesisPage,
                                                       @LoginUser LoginUserDto loginUser) {
        Pageable pageable = PageRequest.of(thesisPage.getPage(), thesisPage.getSize());
        return ApiResponse.success(thesisService.getThesis(pageable, loginUser));
    }

    // 논문 삭제
    @DeleteMapping
    @Operation(summary = "논문 삭제")
    public ApiResponse<Void> deleteThesis(@RequestBody @Valid ThesisDeleteRequest thesisDeleteRequest,
                                          @LoginUser LoginUserDto loginUser) {
        thesisService.deleteThesis(thesisDeleteRequest, loginUser);
        return ApiResponse.success();
    }
}
