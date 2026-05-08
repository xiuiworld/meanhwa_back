package com.example.meanhwa_back.auth.controller;

import com.example.meanhwa_back.auth.dto.DevLoginRequest;
import com.example.meanhwa_back.auth.dto.LogoutRequest;
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

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/{provider}")
    public ApiResponse<TokenResponse> login(
            @PathVariable String provider,
            @Valid @RequestBody DevLoginRequest request
    ) {
        return ApiResponse.ok(authService.login(provider, request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ApiResponse.ok(null);
    }
}
