package com.example.meanhwa_back.log.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * 사용자 행동과 비로그인 이벤트를 저장하는 로그 엔티티.
 * 통계 집계를 위해 actionType, userId, JSON actionData, 발생 시각을 함께 남긴다.
 */
@Entity
@Table(
        name = "action_logs",
        indexes = {
                @Index(name = "idx_action_logs_type_created", columnList = "action_type, created_at"),
                @Index(name = "idx_action_logs_user_created", columnList = "user_id, created_at")
        }
)
public class ActionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private ActionType actionType;

    @Column(name = "action_data", nullable = false, columnDefinition = "TEXT")
    private String actionData;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ActionLog() {
    }

    public ActionLog(Long userId, ActionType actionType, String actionData) {
        this.userId = userId;
        this.actionType = actionType;
        this.actionData = actionData;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getActionData() {
        return actionData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
