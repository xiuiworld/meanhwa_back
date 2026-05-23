package com.example.meanhwa_back.admin.dto.statistics;

import java.time.LocalDate;
/**
 * 관리자 대시보드 상단 요약 지표 응답 DTO.
 * 기간별 행동 로그 카운트와 전체 사용자, 활성 꽃, 좋아요 수를 한 번에 보여준다.
 */

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
