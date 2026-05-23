package com.example.meanhwa_back.flower.dto;

import java.util.List;

import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.ManagementLevel;
import com.example.meanhwa_back.flower.domain.PriceRange;

public record FlowerDetailResponse(
        Long id,
        String name,
        String imageUrl,
        String coreMeaning,
        String description,
        ManagementLevel managementLevel,
        boolean isPetSafe,
        PriceRange priceRange,
        List<TagSummaryResponse> tags
) {
    public static FlowerDetailResponse of(Flower flower, List<TagSummaryResponse> tags) {
        return new FlowerDetailResponse(
                flower.getId(),
                flower.getName(),
                flower.getImageUrl(),
                flower.getCoreMeaning(),
                flower.getDescription(),
                flower.getManagementLevel(),
                !flower.isToxicToPets(),
                flower.getPriceRange(),
                tags
        );
    }
}
