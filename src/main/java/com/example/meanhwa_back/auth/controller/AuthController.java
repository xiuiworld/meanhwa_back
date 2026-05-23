package com.example.meanhwa_back.auth.controller;

import com.example.meanhwa_back.auth.dto.DevLoginRequest;
import com.example.meanhwa_back.auth.dto.LogoutRequest;
import com.example.meanhwa_back.auth.dto.SocialLoginRequest;
import com.example.meanhwa_back.auth.dto.TokenRefreshRequest;
import com.example.meanhwa_back.auth.dto.TokenResponse;
import com.example.meanhwa_back.auth.service.AuthService;
import com.example.meanhwa_back.common.response.ApiResponse;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 로그인·토큰 갱신·로그아웃 REST API. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 로컬과 테스트에서 개발용 사용자를 만들고 토큰을 발급한다.
     */
    @PostMapping("/login/dev")
    public ApiResponse<TokenResponse> devLogin(
            @Valid @RequestBody DevLoginRequest request
    ) {
        return ApiResponse.ok(authService.devLogin(request));
    }

    /**
     * OAuth provider 토큰을 검증해 사용자를 생성하거나 갱신하고 서비스 토큰을 발급한다.
     */
    @PostMapping("/login/{provider}")
    public ApiResponse<TokenResponse> socialLogin(
            @PathVariable String provider,
            @Valid @RequestBody SocialLoginRequest request
    ) {
        return ApiResponse.ok(authService.socialLogin(provider, request));
    }

    /**
     * 유효한 refresh token을 검증하고 새 access/refresh token 쌍을 발급한다.
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    /**
     * 저장된 refresh token을 폐기해 이후 토큰 재발급을 막는다.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ApiResponse.ok(null);
    }
}
