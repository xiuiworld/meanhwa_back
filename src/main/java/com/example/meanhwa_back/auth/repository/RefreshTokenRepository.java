package com.example.meanhwa_back.auth.repository;

import java.util.Optional;

import com.example.meanhwa_back.auth.domain.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
/**
 * RefreshToken 저장소.
 * 원문 토큰이 아닌 해시 기준으로 조회해 재발급과 로그아웃 흐름을 처리한다.
 */

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
