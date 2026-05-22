package com.example.meanhwa_back.message.repository;

import com.example.meanhwa_back.message.domain.UserMessage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {
    Page<UserMessage> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
