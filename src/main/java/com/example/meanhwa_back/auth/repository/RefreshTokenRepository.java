package com.example.meanhwa_back.auth.repository;

import java.util.Optional;

import com.example.meanhwa_back.auth.domain.RefreshToken;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * RefreshToken 저장소.
 * 원문 토큰이 아닌 해시 기준으로 조회해 재발급과 로그아웃 흐름을 처리한다.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * 토큰 회전·로그아웃 중 같은 refresh token의 중복 사용을 직렬화하기 위해 row 잠금을 건다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rt from RefreshToken rt where rt.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);
}
