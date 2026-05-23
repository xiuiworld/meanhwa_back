package com.example.meanhwa_back.auth.service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.example.meanhwa_back.auth.domain.OAuthProvider;
import com.example.meanhwa_back.auth.dto.DevLoginRequest;
import com.example.meanhwa_back.auth.dto.LogoutRequest;
import com.example.meanhwa_back.auth.dto.SocialLoginRequest;
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
import org.springframework.transaction.support.TransactionTemplate;

/**
 * OAuth·개발용 로그인 처리 및 사용자 upsert.
 * prod 환경에서는 dev 로그인을 차단한다.
 */
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final Environment environment;
    private final Map<OAuthProvider, OAuthClient> oauthClients;
    private final TransactionTemplate transactionTemplate;

    public AuthService(
            UserRepository userRepository,
            TokenService tokenService,
            Environment environment,
            List<OAuthClient> oauthClients,
            TransactionTemplate transactionTemplate
    ) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.environment = environment;
        this.transactionTemplate = transactionTemplate;
        this.oauthClients = new EnumMap<>(OAuthProvider.class);
        oauthClients.forEach(client -> this.oauthClients.put(client.getProvider(), client));
    }
/**
 * 로컬과 테스트에서 개발용 사용자를 만들고 토큰을 발급한다.
 */

    @Transactional
    public TokenResponse devLogin(DevLoginRequest request) {
        if (isProdProfile()) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }

        Role role = parseRole(request.role());
        User user = upsertUser(
                OAuthProvider.DEV,
                request.oauthId().trim(),
                normalizeEmail(request.email()),
                normalizeNickname(request.nickname()),
                role,
                true
        );

        return tokenService.issue(user);
    }
/**
 * OAuth provider 토큰을 검증해 사용자를 생성하거나 갱신하고 서비스 토큰을 발급한다.
 */

    public TokenResponse socialLogin(String providerValue, SocialLoginRequest request) {
        OAuthProvider provider = OAuthProvider.from(providerValue);
        if (provider == OAuthProvider.DEV) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }

        OAuthClient oauthClient = oauthClients.get(provider);
        if (oauthClient == null) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }

        OAuthProfile profile = oauthClient.fetchProfile(request.accessToken().trim());
        return transactionTemplate.execute(status -> {
            User user = upsertUser(
                    profile.provider(),
                    profile.oauthId(),
                    normalizeEmail(profile.email()),
                    normalizeNickname(profile.nickname()),
                    Role.ROLE_USER,
                    false
            );
            return tokenService.issue(user);
        });
    }
/**
 * 유효한 refresh token을 검증하고 새 access/refresh token 쌍을 발급한다.
 */

    @Transactional
    public TokenResponse refresh(TokenRefreshRequest request) {
        return tokenService.refresh(request.refreshToken());
    }
/**
 * 저장된 refresh token을 폐기해 이후 토큰 재발급을 막는다.
 */

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

    private User upsertUser(
            OAuthProvider provider,
            String oauthId,
            String email,
            String nickname,
            Role role,
            boolean updateExistingRole
    ) {
        return userRepository.findByProviderAndOauthId(provider, oauthId)
                .map(existingUser -> {
                    Role nextRole = updateExistingRole ? role : existingUser.getRole();
                    existingUser.updateProfile(email, nickname, nextRole);
                    return existingUser;
                })
                .orElseGet(() -> userRepository.save(new User(
                        provider,
                        oauthId,
                        email,
                        nickname,
                        role
                )));
    }
}
