package com.example.meanhwa_back.curation.history.dto;

import java.time.LocalDateTime;
import java.util.List;
/**
 * 사용자 큐레이션 히스토리 상세 응답 DTO.
 * 선택 스냅샷과 추천 스냅샷 전체를 내려 상세 복원에 사용한다.
 */
public record CurationResultDetailResponse(
        Long id,
        String flowVersion,
        List<CurationSelectionSnapshot> selections,
        List<CurationRecommendationSnapshot> recommendations,
        LocalDateTime createdAt
) {
}
