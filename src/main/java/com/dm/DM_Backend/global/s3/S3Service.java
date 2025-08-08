package com.dm.DM_Backend.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${spring.aws.credentials.s3.bucket}")
    private String BUCKET_NAME;

    public String uploadFile(MultipartFile file, String dirName) throws IOException {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return "https://" + BUCKET_NAME + ".s3.ap-northeast-2.amazonaws.com/" + fileName;
    }

    public void deleteFile(String fileUrl) {
        String fileName = extractFileName(fileUrl);

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    public void deleteAllFile(List<String> imageUrls) {
        for (String fileName : imageUrls) {
            deleteFile(fileName);
        }
    }

    // DB에 저장된 전체 URL에서 S3에 저장된 파일명만 추출
    private String extractFileName(String fileUrl) {
        String prefix = "https://" + BUCKET_NAME + ".s3.ap-northeast-2.amazonaws.com/";
        return fileUrl.replace(prefix, "");
    }
}
