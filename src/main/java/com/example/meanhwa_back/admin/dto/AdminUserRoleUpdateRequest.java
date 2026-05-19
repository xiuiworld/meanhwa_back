package com.example.meanhwa_back.admin.dto;

import com.example.meanhwa_back.user.domain.Role;

import jakarta.validation.constraints.NotNull;

/**
 * 관리자 권한 승격·회수 요청.
 * <p>운영 DB에서 {@code UPDATE users SET role = ...} 하던 작업을 API로 대체한다.
 */
public record AdminUserRoleUpdateRequest(
        @NotNull Role role
) {
}
