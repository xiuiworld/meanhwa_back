package com.example.meanhwa_back.curation.dto;

import java.util.List;

import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.PriceRange;
import com.example.meanhwa_back.flower.dto.TagSummaryResponse;

public record CurationFlowerResponse(
        Long flowerId,
        String name,
        String imageUrl,
        String coreMeaning,
        PriceRange priceRange,
        boolean isPetSafe,
        int score,
        List<TagSummaryResponse> matchedTags
) {
    public static CurationFlowerResponse of(Flower flower, int score, List<TagSummaryResponse> matchedTags) {
        return new CurationFlowerResponse(
                flower.getId(),
                flower.getName(),
                flower.getImageUrl(),
                flower.getCoreMeaning(),
                flower.getPriceRange(),
                !flower.isToxicToPets(),
                score,
                matchedTags
        );
    }
}
