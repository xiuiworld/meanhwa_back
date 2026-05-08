package com.example.meanhwa_back.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record DevLoginRequest(
        @NotBlank String oauthId,
        String email,
        String nickname,
        String role
) {
}
