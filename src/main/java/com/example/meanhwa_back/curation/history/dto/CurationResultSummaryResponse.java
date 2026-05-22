package com.example.meanhwa_back.curation.history.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CurationResultSummaryResponse(
        Long id,
        String flowVersion,
        List<CurationSelectionSnapshot> selections,
        List<CurationTopFlowerResponse> topFlowers,
        int resultCount,
        LocalDateTime createdAt
) {
}
