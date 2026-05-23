package com.example.meanhwa_back.curation.history.dto;

import java.time.LocalDateTime;
import java.util.List;
/**
 * 사용자 큐레이션 히스토리 목록 응답 DTO.
 * 목록에서는 상위 추천 꽃 일부와 결과 개수만 제공해 응답 크기를 줄인다.
 */

public record CurationResultSummaryResponse(
        Long id,
        String flowVersion,
        List<CurationSelectionSnapshot> selections,
        List<CurationTopFlowerResponse> topFlowers,
        int resultCount,
        LocalDateTime createdAt
) {
}
