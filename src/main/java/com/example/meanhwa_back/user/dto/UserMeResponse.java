package com.example.meanhwa_back.user.dto;

import com.example.meanhwa_back.auth.domain.OAuthProvider;
import com.example.meanhwa_back.user.domain.Role;
import com.example.meanhwa_back.user.domain.User;
/**
 * 내 정보 조회 API 응답 DTO.
 * 인증된 사용자의 기본 프로필, provider, role 정보를 클라이언트에 전달한다.
 */
public record UserMeResponse(
        Long id,
        OAuthProvider provider,
        String oauthId,
        String email,
        String nickname,
        Role role
) {
    /**
     * 도메인 객체나 스냅샷을 이 API 응답 DTO로 변환한다.
     */
    public static UserMeResponse from(User user) {
        return new UserMeResponse(
                user.getId(),
                user.getProvider(),
                user.getOauthId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole()
        );
    }
}
