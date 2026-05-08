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

    private LocalDateTime deletedAt;

    protected Tag() {
    }

    public Tag(TagCategory category, String name) {
        this.category = category;
        this.name = name;
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
