package com.example.meanhwa_back.auth.dto;

import jakarta.validation.constraints.NotBlank;
/**
 * 로컬과 테스트에서만 허용되는 개발용 로그인 요청.
 * 운영 프로필에서는 서비스 계층에서 차단되며 OAuth provider 연동 없이 토큰 발급 흐름을 검증할 때 사용한다.
 */
public record DevLoginRequest(
        @NotBlank String oauthId,
        String email,
        String nickname,
        String role
) {
}
