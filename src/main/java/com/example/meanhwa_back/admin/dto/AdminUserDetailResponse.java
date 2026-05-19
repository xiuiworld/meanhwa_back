package com.example.meanhwa_back.admin.dto;

import java.time.LocalDateTime;

import com.example.meanhwa_back.auth.domain.OAuthProvider;
import com.example.meanhwa_back.user.domain.Role;
import com.example.meanhwa_back.user.domain.User;

/**
 * 백오피스 회원 상세.
 *
 * @param likeCount    {@code user_likes} 건수 (찜한 식물 수)
 * @param historyCount {@code user_histories} 건수 (최근 본 기록 수, 상한 50 적용 전 raw count)
 */
public record AdminUserDetailResponse(
        Long id,
        OAuthProvider provider,
        String oauthId,
        String email,
        String nickname,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long likeCount,
        long historyCount
) {
    public static AdminUserDetailResponse from(User user, long likeCount, long historyCount) {
        return new AdminUserDetailResponse(
                user.getId(),
                user.getProvider(),
                user.getOauthId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                likeCount,
                historyCount
        );
    }
}
