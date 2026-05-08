package com.example.meanhwa_back.auth.service;

import java.time.LocalDateTime;

import com.example.meanhwa_back.auth.domain.RefreshToken;
import com.example.meanhwa_back.auth.dto.TokenResponse;
import com.example.meanhwa_back.auth.repository.RefreshTokenRepository;
import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.common.security.JwtTokenProvider;
import com.example.meanhwa_back.user.domain.User;

import io.jsonwebtoken.JwtException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashService tokenHashService;

    public TokenService(
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenRepository refreshTokenRepository,
            TokenHashService tokenHashService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenHashService = tokenHashService;
    }

    @Transactional
    public TokenResponse issue(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);
        refreshTokenRepository.save(new RefreshToken(
                user,
                tokenHashService.hash(refreshToken),
                jwtTokenProvider.getRefreshTokenExpiresAt(refreshToken)
        ));
        return TokenResponse.bearer(accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpiresInSeconds());
    }

    @Transactional
    public TokenResponse refresh(String refreshTokenValue) {
        validateRefreshJwt(refreshTokenValue);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHashService.hash(refreshTokenValue))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        LocalDateTime now = LocalDateTime.now();
        if (!refreshToken.isActive(now)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        refreshToken.revoke(now);
        return issue(refreshToken.getUser());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        validateRefreshJwt(refreshTokenValue);

        refreshTokenRepository.findByTokenHash(tokenHashService.hash(refreshTokenValue))
                .filter(refreshToken -> refreshToken.isActive(LocalDateTime.now()))
                .ifPresent(refreshToken -> refreshToken.revoke(LocalDateTime.now()));
    }

    private void validateRefreshJwt(String refreshTokenValue) {
        try {
            jwtTokenProvider.validateRefreshToken(refreshTokenValue);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
