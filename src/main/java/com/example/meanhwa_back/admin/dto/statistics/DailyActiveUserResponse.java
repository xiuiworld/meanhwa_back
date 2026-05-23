package com.example.meanhwa_back.admin.dto.statistics;

import java.time.LocalDate;
/**
 * 관리자 통계의 일별 활성 사용자 수 응답 DTO.
 * 행동 로그에 남은 userId를 날짜별 distinct count로 집계한 결과를 담는다.
 */
public record DailyActiveUserResponse(
        LocalDate date,
        long activeUsers
) {
}
