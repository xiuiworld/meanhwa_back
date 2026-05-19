package com.example.meanhwa_back.admin.controller;

import com.example.meanhwa_back.admin.dto.AdminUserDetailResponse;
import com.example.meanhwa_back.admin.dto.AdminUserRoleUpdateRequest;
import com.example.meanhwa_back.admin.dto.AdminUserSummaryResponse;
import com.example.meanhwa_back.admin.service.AdminUserService;
import com.example.meanhwa_back.common.response.ApiResponse;
import com.example.meanhwa_back.common.response.PageResponse;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 백오피스 회원 관리 API.
 * <p>모든 경로는 {@code SecurityConfig}에서 {@link com.example.meanhwa_back.user.domain.Role#ROLE_ADMIN} JWT가 필요하다.
 * 운영 가이드의 SQL {@code UPDATE users SET role = ...} 를 대체하는 것이 주 목적이다.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * 회원 목록 (페이지).
     *
     * @param keyword  이메일·닉네임·oauthId 부분 검색
     * @param provider DEV, KAKAO, NAVER
     * @param role     ROLE_USER, ROLE_ADMIN
     */
    @GetMapping
    public ApiResponse<PageResponse<AdminUserSummaryResponse>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(adminUserService.listUsers(keyword, provider, role, page, size));
    }

    /** 회원 상세 (찜·최근 본 건수 포함). */
    @GetMapping("/{userId}")
    public ApiResponse<AdminUserDetailResponse> getUser(@PathVariable Long userId) {
        return ApiResponse.ok(adminUserService.getUser(userId));
    }

    /**
     * 권한 변경.
     * <p>대상 사용자는 변경 후 재로그인해야 프론트·JWT에 새 role이 반영된다.
     */
    @PutMapping("/{userId}/role")
    public ApiResponse<AdminUserDetailResponse> updateRole(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserRoleUpdateRequest request
    ) {
        return ApiResponse.ok(adminUserService.updateRole(userId, request));
    }
}
