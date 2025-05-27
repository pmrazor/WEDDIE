package com.ssafy.exhi.domain.notice.service;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MinioImageServiceImpl implements MinioImageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            createBucket(); // 버킷이 없다면 생성
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            fileUpload(file, filename);

            return filename;
        } catch (Exception e) {
            log.error("Error occurred while uploading file", e);
            throw new RuntimeException("Error occurred while uploading file", e);
        }
    }

    private void fileUpload(MultipartFile file, String filename)
            throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        // 파일 업로드
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
    }

    private void createBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }

        } catch (Exception e) {
            log.error("Error occurred while creating bucket: " + bucketName, e);
            throw new RuntimeException("Error occurred while creating bucket", e);
        }
    }

    @Override
    public String getImageUrl(String filename) {
        try {
            // presigned URL의 만료 시간을 설정합니다 (예: 1시간)
            int expiryTime = 3600;

            // presigned URL을 생성합니다
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(filename)
                            .expiry(expiryTime)  // 만료 시간 (초)
                            .build()
            );

            return url;
        } catch (Exception e) {
            log.error("Error occurred while generating presigned URL", e);
            throw new RuntimeException("Error occurred while generating presigned URL", e);
        }
    }

}
