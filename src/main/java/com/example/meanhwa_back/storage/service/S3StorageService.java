package com.example.meanhwa_back.storage.service;

import java.io.IOException;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.storage.config.StorageProperties;
import com.example.meanhwa_back.storage.dto.ImageUploadResponse;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/** AWS S3에 이미지 업로드 (prod·s3 프로필). */
@Service
@Profile({"prod", "s3"})
public class S3StorageService implements StorageService {
    private final StorageProperties storageProperties;
    private final ImageFileValidator imageFileValidator;
    private final StorageKeyGenerator storageKeyGenerator;
    private final S3Client s3Client;

    public S3StorageService(
            StorageProperties storageProperties,
            ImageFileValidator imageFileValidator,
            StorageKeyGenerator storageKeyGenerator
    ) {
        this.storageProperties = storageProperties;
        this.imageFileValidator = imageFileValidator;
        this.storageKeyGenerator = storageKeyGenerator;
        this.s3Client = S3Client.builder()
                .region(Region.of(storageProperties.getS3().getRegion()))
                .build();
    }

    @Override
    public ImageUploadResponse uploadImage(MultipartFile file) {
        imageFileValidator.validate(file);
        String key = storageKeyGenerator.generateImageKey(file);
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(storageProperties.getS3().getBucket())
                            .key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException | S3Exception exception) {
            throw new BusinessException(ErrorCode.UPLOAD_FAILED);
        }

        return new ImageUploadResponse(
                storageProperties.getS3().getPublicBaseUrl().replaceAll("/+$", "") + "/" + key,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()
        );
    }
}
