package com.example.meanhwa_back.user.repository;

import java.util.List;

import com.example.meanhwa_back.user.domain.UserLike;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 꽃 찜 저장소.
 * 찜 목록 조회, 중복 여부 확인, 찜 해제와 개수 집계를 담당한다.
 */
public interface UserLikeRepository extends JpaRepository<UserLike, Long> {
    boolean existsByUserIdAndFlowerId(Long userId, Long flowerId);

    List<UserLike> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserIdAndFlowerId(Long userId, Long flowerId);

    long countByUserId(Long userId);
}
