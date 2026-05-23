package com.example.meanhwa_back.auth.dto;

import jakarta.validation.constraints.NotBlank;
/**
 * access token 재발급 요청 본문.
 * 저장된 refresh token과 대조할 원문 refresh token을 받는다.
 */
public record TokenRefreshRequest(
        @NotBlank String refreshToken
) {
}
