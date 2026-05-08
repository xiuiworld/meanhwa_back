package com.example.meanhwa_back.admin.controller;

import com.example.meanhwa_back.admin.dto.AdminTagRequest;
import com.example.meanhwa_back.admin.dto.AdminTagResponse;
import com.example.meanhwa_back.admin.service.AdminTagService;
import com.example.meanhwa_back.common.response.ApiResponse;

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
@RequestMapping("/api/v1/admin/tags")
public class AdminTagController {
    private final AdminTagService adminTagService;

    public AdminTagController(AdminTagService adminTagService) {
        this.adminTagService = adminTagService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminTagResponse>> createTag(@Valid @RequestBody AdminTagRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(adminTagService.createTag(request)));
    }

    @PutMapping("/{tagId}")
    public ApiResponse<AdminTagResponse> updateTag(
            @PathVariable Long tagId,
            @Valid @RequestBody AdminTagRequest request
    ) {
        return ApiResponse.ok(adminTagService.updateTag(tagId, request));
    }

    @DeleteMapping("/{tagId}")
    public ApiResponse<Void> deleteTag(@PathVariable Long tagId) {
        adminTagService.deleteTag(tagId);
        return ApiResponse.ok(null);
    }
}
