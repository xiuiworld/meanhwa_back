package com.example.meanhwa_back.user.dto;

import com.example.meanhwa_back.auth.domain.OAuthProvider;
import com.example.meanhwa_back.user.domain.Role;
import com.example.meanhwa_back.user.domain.User;

public record UserMeResponse(
        Long id,
        OAuthProvider provider,
        String oauthId,
        String email,
        String nickname,
        Role role
) {
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
