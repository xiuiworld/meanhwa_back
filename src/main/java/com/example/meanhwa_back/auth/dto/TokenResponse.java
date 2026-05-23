package com.example.meanhwa_back.auth.dto;
/**
 * 로그인과 토큰 재발급 API의 토큰 응답 DTO.
 * access token 만료까지 남은 초를 함께 내려 클라이언트 갱신 타이밍을 계산하게 한다.
 */

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds
) {
    public static TokenResponse bearer(String accessToken, String refreshToken, long expiresInSeconds) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresInSeconds);
    }
}
