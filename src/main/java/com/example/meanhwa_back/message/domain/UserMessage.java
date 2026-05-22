package com.example.meanhwa_back.message.domain;

import java.time.LocalDateTime;

import com.example.meanhwa_back.curation.history.domain.UserCurationResult;
import com.example.meanhwa_back.flower.domain.Flower;
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

/** 로그인 사용자가 생성한 메시지 이력. 표시값은 생성 시점 snapshot으로 저장한다. */
@Entity
@Table(
        name = "user_messages",
        indexes = {
                @Index(name = "idx_user_messages_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_user_messages_curation_result", columnList = "curation_result_id")
        }
)
public class UserMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flower_id", nullable = false)
    private Flower flower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curation_result_id")
    private UserCurationResult curationResult;

    @Column(name = "flower_name", nullable = false, length = 100)
    private String flowerName;

    @Column(name = "flower_image_url", length = 255)
    private String flowerImageUrl;

    @Column(name = "core_meaning", length = 100)
    private String coreMeaning;

    @Column(name = "selected_tags", nullable = false, columnDefinition = "TEXT")
    private String selectedTags;

    @Column(name = "sender_name", nullable = false, length = 50)
    private String senderName;

    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected UserMessage() {
    }

    public UserMessage(
            User user,
            Flower flower,
            UserCurationResult curationResult,
            String flowerName,
            String flowerImageUrl,
            String coreMeaning,
            String selectedTags,
            String senderName,
            String receiverName,
            String message
    ) {
        this.user = user;
        this.flower = flower;
        this.curationResult = curationResult;
        this.flowerName = flowerName;
        this.flowerImageUrl = flowerImageUrl;
        this.coreMeaning = coreMeaning;
        this.selectedTags = selectedTags;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public Long getFlowerId() {
        return flower.getId();
    }

    public Long getCurationResultId() {
        return curationResult == null ? null : curationResult.getId();
    }

    public String getFlowerName() {
        return flowerName;
    }

    public String getFlowerImageUrl() {
        return flowerImageUrl;
    }

    public String getCoreMeaning() {
        return coreMeaning;
    }

    public String getSelectedTags() {
        return selectedTags;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
