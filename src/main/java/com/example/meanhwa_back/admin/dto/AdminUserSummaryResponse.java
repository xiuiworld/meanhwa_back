package com.example.meanhwa_back.admin.dto;

import java.time.LocalDateTime;

import com.example.meanhwa_back.auth.domain.OAuthProvider;
import com.example.meanhwa_back.user.domain.Role;
import com.example.meanhwa_back.user.domain.User;

/**
 * 백오피스 회원 목록 한 행.
 * <p>민감 정보는 oauthId까지 포함하나, refresh token 등 인증 비밀은 절대 내려주지 않는다.
 */
public record AdminUserSummaryResponse(
        Long id,
        OAuthProvider provider,
        String oauthId,
        String email,
        String nickname,
        Role role,
        LocalDateTime createdAt
) {
    public static AdminUserSummaryResponse from(User user) {
        return new AdminUserSummaryResponse(
                user.getId(),
                user.getProvider(),
                user.getOauthId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
