package com.dm.dmbackend.domain.llm.thesis.serviceImpl;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.llm.thesis.dto.req.ThesisDeleteRequest;
import com.dm.dmbackend.domain.llm.thesis.dto.res.ThesisResponse;
import com.dm.dmbackend.domain.llm.thesis.entity.Thesis;
import com.dm.dmbackend.domain.llm.thesis.entity.ThesisPage;
import com.dm.dmbackend.domain.llm.thesis.repository.ThesisRepository;
import com.dm.dmbackend.domain.llm.thesis.service.ThesisService;
import com.dm.dmbackend.global.common.response.PageResponse;
import com.dm.dmbackend.global.common.utils.validator.RoleValidator;
import com.dm.dmbackend.global.exception.ReturnCode;
import com.dm.dmbackend.global.exception.ServiceException;
import com.dm.dmbackend.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThesisServiceImpl implements ThesisService {
    private final ThesisRepository thesisRepository;
    private final S3Service s3Service;

    // 논문 업로드
    @Override
    @Transactional
    public void uploadThesis(List<MultipartFile> pdfFiles, LoginUserDto loginUser){
        // ROLE_ADMIN 아닌 경우 예외 처리
        RoleValidator.validateAdmin(loginUser);

        // 이미지 파일 검증
        if (pdfFiles == null || pdfFiles.isEmpty() || pdfFiles.size() > 10) {
            throw new ServiceException(ReturnCode.FILE_UPLOAD_ERROR);
        }
        for (MultipartFile pdfFile : pdfFiles) {
            String pdfUrl = null;
            if (!pdfFile.isEmpty()) {
                try {
                    pdfUrl = s3Service.uploadFile(pdfFile, "thesis-pdfs");
                } catch (IOException e) {
                    throw new ServiceException(ReturnCode.INTERNAL_ERROR);
                }
            }
            Thesis thesis = Thesis.builder()
                    .member(loginUser.ConvertToMember())
                    .thesisPdfUrl(pdfUrl)
                    .build();
            thesisRepository.save(thesis);
        }
    }

    // 논문 조회
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ThesisResponse> getThesis(Pageable pageable, LoginUserDto loginUser){
        // ROLE_ADMIN 아닌 경우 예외 처리
        RoleValidator.validateAdmin(loginUser);
        checkPageSize(pageable.getPageSize());
        return PageResponse.of(
                thesisRepository.findByMemberId(loginUser.getId(), pageable)
                        .map(this::convertToThesisResponse)
        );
    }

    // 논문 삭제
    @Override
    @Transactional
    public void deleteThesis(ThesisDeleteRequest thesisDeleteRequest, LoginUserDto loginUser){
        // ROLE_ADMIN 아닌 경우 예외 처리
        RoleValidator.validateAdmin(loginUser);
        List<Thesis> theses = thesisRepository.findAllByIdIn(thesisDeleteRequest.getThesisIdList());
        for (Thesis thesis : theses) {
            s3Service.deleteFile(thesis.getThesisPdfUrl());
            thesisRepository.delete(thesis);
        }
    }

    // ----------------- 헬퍼 메서드 -----------------

    // 요청 페이지 수 제한
    private void checkPageSize(int pageSize) {
        int maxPageSize = ThesisPage.getMaxPageSize();
        if (pageSize > maxPageSize) {
            throw new ServiceException(ReturnCode.PAGE_REQUEST_FAIL);
        }
    }

    // Thesis를 ThesisResponse로 변환
    public ThesisResponse convertToThesisResponse(Thesis thesis){
        return ThesisResponse.builder()
                .id(thesis.getId())
                .thesisPdfUrl(thesis.getThesisPdfUrl())
                .build();
    }
}
