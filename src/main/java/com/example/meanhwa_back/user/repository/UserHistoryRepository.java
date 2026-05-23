package com.example.meanhwa_back.user.repository;

import java.util.List;
import java.util.Optional;

import com.example.meanhwa_back.user.domain.UserHistory;

import org.springframework.data.jpa.repository.JpaRepository;
/**
 * 사용자 최근 본 꽃 이력 저장소.
 * 마이페이지 조회와 이력 중복 갱신, 오래된 이력 정리에 필요한 쿼리를 제공한다.
 */
public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {
    Optional<UserHistory> findByUserIdAndFlowerId(Long userId, Long flowerId);

    List<UserHistory> findByUserIdOrderByViewedAtDesc(Long userId);

    List<UserHistory> findByUserIdOrderByViewedAtAsc(Long userId);

    long countByUserId(Long userId);

    void deleteByUserId(Long userId);
}
