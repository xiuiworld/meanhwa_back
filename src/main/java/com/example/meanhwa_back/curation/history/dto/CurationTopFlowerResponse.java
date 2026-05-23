package com.example.meanhwa_back.curation.history.dto;
/**
 * 큐레이션 히스토리 목록에서 보여줄 상위 추천 꽃 요약 DTO.
 * 상세 스냅샷에서 목록에 필요한 최소 필드만 추려낸다.
 */

public record CurationTopFlowerResponse(
        int rank,
        Long flowerId,
        String name,
        String imageUrl,
        String coreMeaning
) {
    /**
     * 도메인 객체나 스냅샷을 이 API 응답 DTO로 변환한다.
     */
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
