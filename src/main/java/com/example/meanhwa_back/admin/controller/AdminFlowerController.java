package com.example.meanhwa_back.admin.controller;

import com.example.meanhwa_back.admin.dto.AdminFlowerRequest;
import com.example.meanhwa_back.admin.dto.FlowerTagMappingUpdateRequest;
import com.example.meanhwa_back.admin.service.AdminFlowerService;
import com.example.meanhwa_back.common.response.ApiResponse;
import com.example.meanhwa_back.flower.dto.FlowerDetailResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/flowers")
public class AdminFlowerController {
    private final AdminFlowerService adminFlowerService;

    public AdminFlowerController(AdminFlowerService adminFlowerService) {
        this.adminFlowerService = adminFlowerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FlowerDetailResponse>> createFlower(@Valid @RequestBody AdminFlowerRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(adminFlowerService.createFlower(request)));
    }

    @PutMapping("/{flowerId}")
    public ApiResponse<FlowerDetailResponse> updateFlower(
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
    public ApiResponse<FlowerDetailResponse> replaceMappings(
            @PathVariable Long flowerId,
            @Valid @RequestBody FlowerTagMappingUpdateRequest request
    ) {
        return ApiResponse.ok(adminFlowerService.replaceMappings(flowerId, request));
    }
}
