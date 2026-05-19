package com.example.meanhwa_back.storage.controller;

import com.example.meanhwa_back.common.response.ApiResponse;
import com.example.meanhwa_back.storage.dto.ImageUploadResponse;
import com.example.meanhwa_back.storage.service.StorageService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 관리자 이미지 업로드 API (S3 또는 로컬 fake). */
@RestController
@RequestMapping("/api/v1/admin/uploads")
public class AdminUploadController {
    private final StorageService storageService;

    public AdminUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/images")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(storageService.uploadImage(file)));
    }
}
