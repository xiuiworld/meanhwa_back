package com.example.meanhwa_back.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
        @NotBlank String accessToken
) {
}
