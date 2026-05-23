package com.example.meanhwa_back.curation.wizard.dto;

import java.util.List;

import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.curation.dto.CurationFlowerResponse;

public record CurationResultsResponse(
        List<CurationFlowerResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        Long curationResultId
) {
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
