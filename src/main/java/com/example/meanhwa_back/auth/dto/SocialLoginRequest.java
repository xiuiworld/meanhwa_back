package com.example.meanhwa_back.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 카카오와 네이버 소셜 로그인 요청 본문.
 * 클라이언트가 provider에서 받은 access token을 서버 검증용으로 전달한다.
 */
public record SocialLoginRequest(
        @NotBlank String accessToken
) {
}
