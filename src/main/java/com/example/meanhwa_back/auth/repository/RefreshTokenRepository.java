package com.example.meanhwa_back.auth.repository;

import java.util.Optional;

import com.example.meanhwa_back.auth.domain.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
