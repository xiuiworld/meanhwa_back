package com.example.meanhwa_back.tag.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 큐레이션·메시지 생성에 사용하는 태그. soft delete 지원. */
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TagCategory category;

    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 위저드·API 공통 식별 코드 (예: {@code BIRTHDAY}, {@code LOVE_3}).
     * 레거시 태그(스타일·계절 등)는 null 가능.
     */
    @Column(length = 80, unique = true)
    private String code;

    private LocalDateTime deletedAt;

    protected Tag() {
    }

    public Tag(TagCategory category, String name) {
        this.category = category;
        this.name = name;
    }

    public Tag(TagCategory category, String name, String code) {
        this.category = category;
        this.name = name;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public TagCategory getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void update(TagCategory category, String name) {
        this.category = category;
        this.name = name;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
