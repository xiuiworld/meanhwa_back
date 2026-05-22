package com.example.meanhwa_back.curation.history.repository;

import java.util.Optional;

import com.example.meanhwa_back.curation.history.domain.UserCurationResult;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCurationResultRepository extends JpaRepository<UserCurationResult, Long> {
    Page<UserCurationResult> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<UserCurationResult> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<UserCurationResult> findByIdAndUserId(Long id, Long userId);
}
