package com.example.meanhwa_back.curation.wizard.dto;

import java.util.List;

import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.curation.dto.CurationFlowerResponse;
/**
 * 큐레이션 위저드 제출 결과 응답 DTO.
 * 선택한 flowVersion, selections, 추천 결과 페이지를 한 번에 반환한다.
 */

public record CurationResultsResponse(
        List<CurationFlowerResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        Long curationResultId
) {
    /**
     * 도메인 객체나 스냅샷을 이 API 응답 DTO로 변환한다.
     */
    public static CurationResultsResponse from(
            PageResponse<CurationFlowerResponse> page,
            Long curationResultId
    ) {
        return new CurationResultsResponse(
                page.content(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.hasNext(),
                curationResultId
        );
    }
}
