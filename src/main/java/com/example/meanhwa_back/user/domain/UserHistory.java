package com.example.meanhwa_back.user.domain;

import java.time.LocalDateTime;

import com.example.meanhwa_back.flower.domain.Flower;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** 사용자별 꽃 상세 조회 이력. */
@Entity
@Table(
        name = "user_histories",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_histories_user_flower", columnNames = {"user_id", "flower_id"}),
        indexes = @Index(name = "idx_user_histories_user_viewed", columnList = "user_id, viewed_at")
)
/**
 * 사용자가 최근 본 꽃 기록 엔티티.
 * 동일 꽃 조회는 viewedAt을 갱신하고 오래된 이력은 서비스 계층에서 제한 개수만 유지한다.
 */
public class UserHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flower_id", nullable = false)
    private Flower flower;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    protected UserHistory() {
    }

    public UserHistory(User user, Flower flower, LocalDateTime viewedAt) {
        this.user = user;
        this.flower = flower;
        this.viewedAt = viewedAt;
    }
/**
 * 최근 본 꽃 이력의 조회 시각을 현재 시간으로 갱신한다.
 */

    public void updateViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }

    public Flower getFlower() {
        return flower;
    }
}
