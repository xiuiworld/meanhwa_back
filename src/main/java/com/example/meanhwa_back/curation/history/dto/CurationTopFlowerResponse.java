package com.example.meanhwa_back.curation.history.dto;

public record CurationTopFlowerResponse(
        int rank,
        Long flowerId,
        String name,
        String imageUrl,
        String coreMeaning
) {
    public static CurationTopFlowerResponse from(CurationRecommendationSnapshot recommendation) {
        return new CurationTopFlowerResponse(
                recommendation.rank(),
                recommendation.flowerId(),
                recommendation.name(),
                recommendation.imageUrl(),
                recommendation.coreMeaning()
        );
    }
}
