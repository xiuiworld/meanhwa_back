package com.example.meanhwa_back.admin.controller;

import com.example.meanhwa_back.admin.dto.AdminFlowerRequest;
import com.example.meanhwa_back.admin.dto.AdminFlowerDetailResponse;
import com.example.meanhwa_back.admin.dto.FlowerTagMappingUpdateRequest;
import com.example.meanhwa_back.admin.service.AdminFlowerService;
import com.example.meanhwa_back.common.response.ApiResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 관리자용 꽃 CRUD 및 태그 매핑 API. */
@RestController
@RequestMapping("/api/v1/admin/flowers")
public class AdminFlowerController {
    private final AdminFlowerService adminFlowerService;

    public AdminFlowerController(AdminFlowerService adminFlowerService) {
        this.adminFlowerService = adminFlowerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminFlowerDetailResponse>> createFlower(@Valid @RequestBody AdminFlowerRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(adminFlowerService.createFlower(request)));
    }

    @GetMapping("/{flowerId}")
    public ApiResponse<AdminFlowerDetailResponse> getFlower(@PathVariable Long flowerId) {
        return ApiResponse.ok(adminFlowerService.getFlower(flowerId));
    }

    @PutMapping("/{flowerId}")
    public ApiResponse<AdminFlowerDetailResponse> updateFlower(
            @PathVariable Long flowerId,
            @Valid @RequestBody AdminFlowerRequest request
    ) {
        return ApiResponse.ok(adminFlowerService.updateFlower(flowerId, request));
    }

    @DeleteMapping("/{flowerId}")
    public ApiResponse<Void> deleteFlower(@PathVariable Long flowerId) {
        adminFlowerService.deleteFlower(flowerId);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{flowerId}/tags")
    public ApiResponse<AdminFlowerDetailResponse> replaceMappings(
            @PathVariable Long flowerId,
            @Valid @RequestBody FlowerTagMappingUpdateRequest request
    ) {
        return ApiResponse.ok(adminFlowerService.replaceMappings(flowerId, request));
    }
}
