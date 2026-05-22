package com.example.meanhwa_back.curation.history.dto;

import java.util.List;

import com.example.meanhwa_back.flower.domain.PriceRange;
import com.example.meanhwa_back.flower.dto.TagSummaryResponse;

public record CurationRecommendationSnapshot(
        int rank,
        Long flowerId,
        String name,
        String imageUrl,
        String coreMeaning,
        PriceRange priceRange,
        boolean isPetSafe,
        int score,
        String recommendationReason,
        List<TagSummaryResponse> matchedTags
) {
}
