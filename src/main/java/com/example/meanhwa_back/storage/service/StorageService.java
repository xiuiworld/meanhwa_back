package com.example.meanhwa_back.storage.service;

import com.example.meanhwa_back.storage.dto.ImageUploadResponse;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    ImageUploadResponse uploadImage(MultipartFile file);
}
