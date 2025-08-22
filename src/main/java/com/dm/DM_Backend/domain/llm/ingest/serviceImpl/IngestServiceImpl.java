package com.dm.DM_Backend.domain.llm.ingest.serviceImpl;

import com.dm.DM_Backend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.DM_Backend.domain.account.member.entity.Member;
import com.dm.DM_Backend.domain.llm.ingest.service.IngestService;
import com.dm.DM_Backend.domain.llm.thesis.dto.res.ThesisResponse;
import com.dm.DM_Backend.domain.llm.thesis.entity.Thesis;
import com.dm.DM_Backend.domain.llm.thesis.repository.ThesisRepository;
import com.dm.DM_Backend.global.exception.ReturnCode;
import com.dm.DM_Backend.global.exception.ServiceException;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestServiceImpl implements IngestService {
    private final EmbeddingModel embeddingModel;
    private final PgVectorEmbeddingStore embeddingStore;
    private final ThesisRepository thesisRepository;
    private final JdbcTemplate jdbcTemplate;

    // 논문 임베딩
    @Override
    @Transactional
    public void ingestAll(LoginUserDto loginUser) {
        // ROLE_ADMIN 검증
        validateAdminRole(loginUser);
        List<Thesis> allTheses = thesisRepository.findAll();
        for (Thesis thesis : allTheses) {
            String pdfUrl = thesis.getThesisPdfUrl();
            if (pdfUrl != null && !pdfUrl.isBlank()) {
                ingestFromUrl(pdfUrl);
            }
        }
    }

    // 논문Url 임베딩
    private void ingestFromUrl(String pdfUrl) {
        try {
            Path tempFile = Files.createTempFile("thesis-", ".pdf");

            // baseUrl과 fileName 분리
            int lastSlash = pdfUrl.lastIndexOf('/');
            String baseUrl = pdfUrl.substring(0, lastSlash + 1);
            String fileName = pdfUrl.substring(lastSlash + 1);

            // 파일명만 인코딩
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            String encodedUrl = baseUrl + encodedFileName;

            // 이제 안전하게 openStream 가능
            try (InputStream in = new URL(encodedUrl).openStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            ingestFromPdf(tempFile);
            Files.deleteIfExists(tempFile);
        } catch (Exception e) {
            log.error("Ingest 실패: {} , 원인: {}", pdfUrl, e.getMessage(), e);
            throw new RuntimeException("Ingest 실패: " + pdfUrl, e);
        }
    }

    // 논문PDF 임베딩
    private void ingestFromPdf(Path pdfPath) {
        DocumentSplitter splitter = DocumentSplitters.recursive(300, 50);
        var ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .documentSplitter(splitter)
                .build();
        var doc = FileSystemDocumentLoader.loadDocument(
                pdfPath,
                new ApachePdfBoxDocumentParser()
        );
        ingestor.ingest(doc);
    }

    // 임베딩된 논문 개수 조회
    @Override
    @Transactional(readOnly = true)
    public long getEmbeddingCount(LoginUserDto loginUser) {
        validateAdminRole(loginUser);
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM screen_time_embeddings", Long.class);
        return count != null ? count : 0L;
    }

    // 임베딩된 논문 전체 삭제
    @Override
    @Transactional
    public void clearAll(LoginUserDto loginUser) {
        // ROLE_ADMIN 검증
        validateAdminRole(loginUser);
        jdbcTemplate.update("TRUNCATE TABLE screen_time_embeddings");
    }

    // ----------------- 헬퍼 메서드 -----------------

    // ROLE_ADMIN 아닌 경우 예외 처리
    public static void validateAdminRole(LoginUserDto loginUser) {
        if (loginUser.getRole() != Member.MemberRole.ROLE_ADMIN) {
            throw new ServiceException(ReturnCode.NOT_AUTHORIZED);
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
