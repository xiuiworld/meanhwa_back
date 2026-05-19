package com.example.meanhwa_back.storage.service;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** S3 객체 키(경로) 생성. */
@Component
public class StorageKeyGenerator {
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
