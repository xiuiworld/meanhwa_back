package com.example.meanhwa_back.storage.service;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** S3 객체 키(경로) 생성. */
@Component
public class StorageKeyGenerator {
    /**
     * 업로드 이미지가 충돌 없이 저장되도록 날짜와 UUID 기반 object key를 만든다.
     */
    public String generateImageKey(MultipartFile file) {
        return "flowers/" + UUID.randomUUID() + extension(file.getContentType());
    }

    private String extension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}
