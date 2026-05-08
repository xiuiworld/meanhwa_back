package com.example.meanhwa_back.storage.dto;

public record ImageUploadResponse(
        String imageUrl,
        String originalFilename,
        String contentType,
        long size
) {
}
