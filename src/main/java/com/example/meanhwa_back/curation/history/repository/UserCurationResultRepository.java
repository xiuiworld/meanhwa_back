package com.example.meanhwa_back.curation.history.repository;

import java.util.Optional;

import com.example.meanhwa_back.curation.history.domain.UserCurationResult;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * 사용자 큐레이션 결과 히스토리 저장소.
 * 사용자 소유권 조건을 포함한 조회 메서드로 다른 사용자의 결과 접근을 방지한다.
 */

public interface UserCurationResultRepository extends JpaRepository<UserCurationResult, Long> {
    Page<UserCurationResult> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<UserCurationResult> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<UserCurationResult> findByIdAndUserId(Long id, Long userId);
}
