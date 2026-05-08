package com.example.meanhwa_back.auth.service;

import java.util.Arrays;

import com.example.meanhwa_back.auth.domain.OAuthProvider;
import com.example.meanhwa_back.auth.dto.DevLoginRequest;
import com.example.meanhwa_back.auth.dto.LogoutRequest;
import com.example.meanhwa_back.auth.dto.TokenRefreshRequest;
import com.example.meanhwa_back.auth.dto.TokenResponse;
import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.user.domain.Role;
import com.example.meanhwa_back.user.domain.User;
import com.example.meanhwa_back.user.repository.UserRepository;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final Environment environment;

    public AuthService(
            UserRepository userRepository,
            TokenService tokenService,
            Environment environment
    ) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.environment = environment;
    }

    @Transactional
    public TokenResponse login(String providerValue, DevLoginRequest request) {
        OAuthProvider provider = OAuthProvider.from(providerValue);
        if (provider != OAuthProvider.DEV || isProdProfile()) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }

        Role role = parseRole(request.role());
        String nickname = normalizeNickname(request.nickname());
        User user = userRepository.findByProviderAndOauthId(provider, request.oauthId().trim())
                .map(existingUser -> {
                    existingUser.updateProfile(normalizeEmail(request.email()), nickname, role);
                    return existingUser;
                })
                .orElseGet(() -> userRepository.save(new User(
                        provider,
                        request.oauthId().trim(),
                        normalizeEmail(request.email()),
                        nickname,
                        role
                )));

        return tokenService.issue(user);
    }

    @Transactional
    public TokenResponse refresh(TokenRefreshRequest request) {
        return tokenService.refresh(request.refreshToken());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        tokenService.logout(request.refreshToken());
    }

    private boolean isProdProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    private Role parseRole(String value) {
        if (value == null || value.isBlank()) {
            return Role.ROLE_USER;
        }

        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "유효하지 않은 role입니다.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim();
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return "민화유저";
        }
        return nickname.trim();
    }
}
