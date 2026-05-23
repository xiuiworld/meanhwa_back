package com.example.meanhwa_back.storage.dto;
/**
 * 관리자 이미지 업로드 성공 응답 DTO.
 * 저장된 공개 URL과 원본 파일 메타데이터를 관리자 화면에 돌려준다.
 */

public record ImageUploadResponse(
        String imageUrl,
        String originalFilename,
        String contentType,
        long size
) {
}
