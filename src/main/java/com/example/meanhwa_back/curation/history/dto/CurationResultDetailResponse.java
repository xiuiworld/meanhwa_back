package com.example.meanhwa_back.curation.history.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CurationResultDetailResponse(
        Long id,
        String flowVersion,
        List<CurationSelectionSnapshot> selections,
        List<CurationRecommendationSnapshot> recommendations,
        LocalDateTime createdAt
) {
}
