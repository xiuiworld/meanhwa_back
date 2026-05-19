package com.example.meanhwa_back.storage.service;

import com.example.meanhwa_back.storage.dto.ImageUploadResponse;

import org.springframework.web.multipart.MultipartFile;

/** 이미지 업로드 추상화 (S3 / 로컬 fake 구현). */
public interface StorageService {
    ImageUploadResponse uploadImage(MultipartFile file);
}
