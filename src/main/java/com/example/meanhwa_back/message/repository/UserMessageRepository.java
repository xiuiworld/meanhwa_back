package com.example.meanhwa_back.message.repository;

import com.example.meanhwa_back.message.domain.UserMessage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * 사용자가 생성한 메시지 저장소.
 * 마이페이지 메시지 목록을 생성 최신순 페이지로 조회한다.
 */

public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {
    Page<UserMessage> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
