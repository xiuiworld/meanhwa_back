package com.example.meanhwa_back.admin.controller;

import com.example.meanhwa_back.admin.dto.AdminTagRequest;
import com.example.meanhwa_back.admin.dto.AdminTagResponse;
import com.example.meanhwa_back.admin.service.AdminTagService;
import com.example.meanhwa_back.common.response.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 관리자용 태그 CRUD API. */
@RestController
@RequestMapping("/api/v1/admin/tags")
@Validated
public class AdminTagController {
    private final AdminTagService adminTagService;

    public AdminTagController(AdminTagService adminTagService) {
        this.adminTagService = adminTagService;
    }

    /**
     * 관리자 요청으로 새 태그를 생성하고 중복 이름을 방지한다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AdminTagResponse>> createTag(@Valid @RequestBody AdminTagRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(adminTagService.createTag(request)));
    }

    /**
     * 기존 태그의 카테고리와 이름을 수정한다.
     */
    @PutMapping("/{tagId}")
    public ApiResponse<AdminTagResponse> updateTag(
            @PathVariable @Positive Long tagId,
            @Valid @RequestBody AdminTagRequest request
    ) {
        return ApiResponse.ok(adminTagService.updateTag(tagId, request));
    }

    /**
     * 태그를 soft delete 처리해 공개 API와 큐레이션 대상에서 제외한다.
     */
    @DeleteMapping("/{tagId}")
    public ApiResponse<Void> deleteTag(@PathVariable @Positive Long tagId) {
        adminTagService.deleteTag(tagId);
        return ApiResponse.ok(null);
    }
}
