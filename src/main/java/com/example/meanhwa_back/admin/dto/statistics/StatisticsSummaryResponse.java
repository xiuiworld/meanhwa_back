package com.example.meanhwa_back.admin.dto.statistics;

import java.time.LocalDate;

public record StatisticsSummaryResponse(
        LocalDate from,
        LocalDate to,
        long totalUsers,
        long activeFlowers,
        long totalLikes,
        long curationCount,
        long searchCount,
        long detailViewCount,
        long curationClickCount
) {
}
