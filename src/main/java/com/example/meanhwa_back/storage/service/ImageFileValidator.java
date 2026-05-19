package com.example.meanhwa_back.storage.service;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.storage.config.StorageProperties;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** 업로드 이미지 MIME·용량 검증. */
@Component
public class ImageFileValidator {
    private final StorageProperties storageProperties;

    public ImageFileValidator(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "업로드할 이미지 파일이 필요합니다.");
        }
        if (file.getSize() > storageProperties.getMaxFileSizeBytes()) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        if (!storageProperties.getAllowedContentTypes().contains(file.getContentType())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }
}
