package com.example.meanhwa_back.curation.history.domain;

import java.time.LocalDateTime;

import com.example.meanhwa_back.user.domain.User;

import org.hibernate.annotations.CreationTimestamp;

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

/** 로그인 사용자의 큐레이션 완료 결과 snapshot. */
@Entity
@Table(
        name = "user_curation_results",
        indexes = @Index(name = "idx_user_curation_results_user_created", columnList = "user_id, created_at")
)
/**
 * 로그인 사용자가 저장한 큐레이션 결과 스냅샷 엔티티.
 * 선택 항목과 추천 결과를 JSON으로 보존해 이후 태그나 꽃 데이터가 바뀌어도 당시 결과를 재현한다.
 */
public class UserCurationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "flow_version", nullable = false, length = 50)
    private String flowVersion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String selections;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "result_count", nullable = false)
    private int resultCount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected UserCurationResult() {
    }

    public UserCurationResult(
            User user,
            String flowVersion,
            String selections,
            String recommendations,
            int resultCount
    ) {
        this.user = user;
        this.flowVersion = flowVersion;
        this.selections = selections;
        this.recommendations = recommendations;
        this.resultCount = resultCount;
    }

    public Long getId() {
        return id;
    }

    public String getFlowVersion() {
        return flowVersion;
    }

    public String getSelections() {
        return selections;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public int getResultCount() {
        return resultCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
