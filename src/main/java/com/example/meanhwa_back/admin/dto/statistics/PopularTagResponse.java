package com.example.meanhwa_back.admin.dto.statistics;

import com.example.meanhwa_back.tag.domain.TagCategory;
/**
 * 관리자 통계의 인기 태그 응답 DTO.
 * 큐레이션 시작 로그에 포함된 선택 태그 빈도를 태그 메타데이터와 함께 내려준다.
 */
public record PopularTagResponse(
        Long tagId,
        TagCategory category,
        String name,
        long count
) {
}
