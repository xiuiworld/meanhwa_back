package com.example.meanhwa_back.user.repository;

import java.util.List;
import java.util.Optional;

import com.example.meanhwa_back.user.domain.UserHistory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {
    Optional<UserHistory> findByUserIdAndFlowerId(Long userId, Long flowerId);

    List<UserHistory> findByUserIdOrderByViewedAtDesc(Long userId);

    List<UserHistory> findByUserIdOrderByViewedAtAsc(Long userId);

    long countByUserId(Long userId);

    void deleteByUserId(Long userId);
}
