package com.example.meanhwa_back.admin.dto;

import java.util.List;

import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.ManagementLevel;
import com.example.meanhwa_back.flower.domain.PriceRange;

public record AdminFlowerDetailResponse(
        Long id,
        String name,
        String imageUrl,
        String coreMeaning,
        ManagementLevel managementLevel,
        String managementInfo,
        boolean isPetSafe,
        PriceRange priceRange,
        List<AdminFlowerTagMappingResponse> tags
) {
    public static AdminFlowerDetailResponse of(Flower flower, List<AdminFlowerTagMappingResponse> tags) {
        return new AdminFlowerDetailResponse(
                flower.getId(),
                flower.getName(),
                flower.getImageUrl(),
                flower.getCoreMeaning(),
                flower.getManagementLevel(),
                flower.getManagementInfo(),
                !flower.isToxicToPets(),
                flower.getPriceRange(),
                tags
        );
    }
}
