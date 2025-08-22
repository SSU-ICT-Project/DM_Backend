package com.dm.DM_Backend.domain.llm.thesis.service;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.llm.thesis.dto.req.ThesisDeleteRequest;
import com.dm.DM_Backend.domain.llm.thesis.dto.res.ThesisResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ThesisService {
    // 논문 업로드
    void uploadThesis(List<MultipartFile> pdfFiles, LoginUserDto loginUser);

    // 논문 조회
    Page<ThesisResponse> getThesis(Pageable pageable, LoginUserDto loginUser);

    // 논문 삭제
    void deleteThesis(ThesisDeleteRequest thesisDeleteRequest, LoginUserDto loginUser);
}
