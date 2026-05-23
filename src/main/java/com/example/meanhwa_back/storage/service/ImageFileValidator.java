package com.example.meanhwa_back.storage.service;

import java.io.IOException;
import java.util.Locale;

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

    /**
     * 업로드 파일의 존재 여부, 크기, content type, 실제 파일 헤더를 검증한다.
     */
    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "업로드할 이미지 파일이 필요합니다.");
        }
        if (file.getSize() > storageProperties.getMaxFileSizeBytes()) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!isAllowedContentType(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
        if (!matchesMagicBytes(file, contentType)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
        return contentType.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedContentType(String contentType) {
        return storageProperties.getAllowedContentTypes()
                .stream()
                .map(this::normalizeContentType)
                .anyMatch(contentType::equals);
    }

    private boolean matchesMagicBytes(MultipartFile file, String contentType) {
        byte[] header = readHeader(file);
        return switch (contentType) {
            case "image/jpeg" -> isJpeg(header);
            case "image/png" -> isPng(header);
            case "image/webp" -> isWebp(header);
            default -> false;
        };
    }

    private byte[] readHeader(MultipartFile file) {
        try {
            return file.getInputStream().readNBytes(12);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private boolean isJpeg(byte[] header) {
        return header.length >= 3
                && unsigned(header[0]) == 0xFF
                && unsigned(header[1]) == 0xD8
                && unsigned(header[2]) == 0xFF;
    }

    private boolean isPng(byte[] header) {
        return header.length >= 8
                && unsigned(header[0]) == 0x89
                && unsigned(header[1]) == 0x50
                && unsigned(header[2]) == 0x4E
                && unsigned(header[3]) == 0x47
                && unsigned(header[4]) == 0x0D
                && unsigned(header[5]) == 0x0A
                && unsigned(header[6]) == 0x1A
                && unsigned(header[7]) == 0x0A;
    }

    private boolean isWebp(byte[] header) {
        return header.length >= 12
                && header[0] == 'R'
                && header[1] == 'I'
                && header[2] == 'F'
                && header[3] == 'F'
                && header[8] == 'W'
                && header[9] == 'E'
                && header[10] == 'B'
                && header[11] == 'P';
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }
}
