package com.example.meanhwa_back.curation.history.dto;

import java.util.List;

import com.example.meanhwa_back.flower.domain.PriceRange;
import com.example.meanhwa_back.flower.dto.TagSummaryResponse;
/**
 * 큐레이션 결과 저장 시점의 추천 꽃 스냅샷.
 * 히스토리 상세 조회가 현재 꽃 데이터 변경에 흔들리지 않도록 화면 표시값을 보존한다.
 */

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
