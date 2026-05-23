package com.example.meanhwa_back.admin.controller;

import com.example.meanhwa_back.admin.dto.AdminFlowerRequest;
import com.example.meanhwa_back.admin.dto.AdminFlowerDetailResponse;
import com.example.meanhwa_back.admin.dto.FlowerTagMappingUpdateRequest;
import com.example.meanhwa_back.admin.service.AdminFlowerService;
import com.example.meanhwa_back.common.response.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@Validated
public class AdminFlowerController {
    private final AdminFlowerService adminFlowerService;

    public AdminFlowerController(AdminFlowerService adminFlowerService) {
        this.adminFlowerService = adminFlowerService;
    }

    /**
     * 관리자 요청을 검증한 뒤 새 꽃 데이터를 생성한다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AdminFlowerDetailResponse>> createFlower(@Valid @RequestBody AdminFlowerRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(adminFlowerService.createFlower(request)));
    }

    @GetMapping("/{flowerId}")
    public ApiResponse<AdminFlowerDetailResponse> getFlower(@PathVariable @Positive Long flowerId) {
        return ApiResponse.ok(adminFlowerService.getFlower(flowerId));
    }

    /**
     * 관리자 요청값으로 기존 꽃 데이터를 갱신한다.
     */
    @PutMapping("/{flowerId}")
    public ApiResponse<AdminFlowerDetailResponse> updateFlower(
            @PathVariable @Positive Long flowerId,
            @Valid @RequestBody AdminFlowerRequest request
    ) {
        return ApiResponse.ok(adminFlowerService.updateFlower(flowerId, request));
    }

    /**
     * 꽃을 물리 삭제하지 않고 soft delete 처리한다.
     */
    @DeleteMapping("/{flowerId}")
    public ApiResponse<Void> deleteFlower(@PathVariable @Positive Long flowerId) {
        adminFlowerService.deleteFlower(flowerId);
        return ApiResponse.ok(null);
    }

    /**
     * 특정 꽃의 태그 매핑을 요청 목록 기준으로 일괄 교체한다.
     */
    @PutMapping("/{flowerId}/tags")
    public ApiResponse<AdminFlowerDetailResponse> replaceMappings(
            @PathVariable @Positive Long flowerId,
            @Valid @RequestBody FlowerTagMappingUpdateRequest request
    ) {
        return ApiResponse.ok(adminFlowerService.replaceMappings(flowerId, request));
    }
}
