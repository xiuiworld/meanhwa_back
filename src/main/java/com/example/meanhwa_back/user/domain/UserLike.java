package com.example.meanhwa_back.user.domain;

import java.time.LocalDateTime;

import com.example.meanhwa_back.flower.domain.Flower;

import org.hibernate.annotations.CreationTimestamp;

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

@Entity
@Table(
        name = "user_likes",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_likes_user_flower", columnNames = {"user_id", "flower_id"}),
        indexes = @Index(name = "idx_user_likes_user_created", columnList = "user_id, created_at")
)
public class UserLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flower_id", nullable = false)
    private Flower flower;

    @CreationTimestamp
    private LocalDateTime createdAt;

    protected UserLike() {
    }

    public UserLike(User user, Flower flower) {
        this.user = user;
        this.flower = flower;
    }

    public Flower getFlower() {
        return flower;
    }
}
