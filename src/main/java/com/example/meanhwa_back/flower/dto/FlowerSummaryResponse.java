package com.example.meanhwa_back.flower.dto;

import java.io.Serializable;

import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.ManagementLevel;
import com.example.meanhwa_back.flower.domain.PriceRange;

public record FlowerSummaryResponse(
        Long id,
        String name,
        String imageUrl,
        String coreMeaning,
        ManagementLevel managementLevel,
        boolean isPetSafe,
        PriceRange priceRange
) implements Serializable {
    public static FlowerSummaryResponse from(Flower flower) {
        return new FlowerSummaryResponse(
                flower.getId(),
                flower.getName(),
                flower.getImageUrl(),
                flower.getCoreMeaning(),
                flower.getManagementLevel(),
                !flower.isToxicToPets(),
                flower.getPriceRange()
        );
    }
}
