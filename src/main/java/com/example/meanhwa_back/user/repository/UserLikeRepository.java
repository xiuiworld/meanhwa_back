package com.example.meanhwa_back.user.repository;

import java.util.List;

import com.example.meanhwa_back.user.domain.UserLike;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {
    boolean existsByUserIdAndFlowerId(Long userId, Long flowerId);

    List<UserLike> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserIdAndFlowerId(Long userId, Long flowerId);

    long countByUserId(Long userId);
}
