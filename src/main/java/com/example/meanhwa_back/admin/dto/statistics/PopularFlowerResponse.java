package com.example.meanhwa_back.admin.dto.statistics;
/**
 * 관리자 통계의 인기 꽃 응답 DTO.
 * 상세 조회와 큐레이션 클릭 로그를 합산한 count와 화면 표시용 꽃 정보를 함께 제공한다.
 */

public record PopularFlowerResponse(
        Long flowerId,
        String name,
        String imageUrl,
        long count
) {
}
