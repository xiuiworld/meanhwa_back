package com.example.meanhwa_back.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그아웃 요청 본문.
 * 전달된 refresh token을 해시로 찾아 폐기해 이후 재발급을 막는다.
 */
public record LogoutRequest(
        @NotBlank String refreshToken
) {
}
