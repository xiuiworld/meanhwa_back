package com.example.meanhwa_back.auth.service;

import com.example.meanhwa_back.auth.domain.OAuthProvider;

public record OAuthProfile(
        OAuthProvider provider,
        String oauthId,
        String email,
        String nickname
) {
}
