package com.example.meanhwa_back.flower.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "flowers")
public class Flower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String imageUrl;

    @Column(length = 100)
    private String coreMeaning;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ManagementLevel managementLevel;

    @Column(columnDefinition = "TEXT")
    private String managementInfo;

    @Column(name = "is_toxic_to_pets", nullable = false)
    private boolean isToxicToPets;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriceRange priceRange;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Flower() {
    }

    public Flower(
            String name,
            String imageUrl,
            String coreMeaning,
            ManagementLevel managementLevel,
            String managementInfo,
            boolean isToxicToPets,
            PriceRange priceRange
    ) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.coreMeaning = coreMeaning;
        this.managementLevel = managementLevel;
        this.managementInfo = managementInfo;
        this.isToxicToPets = isToxicToPets;
        this.priceRange = priceRange;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCoreMeaning() {
        return coreMeaning;
    }

    public ManagementLevel getManagementLevel() {
        return managementLevel;
    }

    public String getManagementInfo() {
        return managementInfo;
    }

    public boolean isToxicToPets() {
        return isToxicToPets;
    }

    public PriceRange getPriceRange() {
        return priceRange;
    }
}
