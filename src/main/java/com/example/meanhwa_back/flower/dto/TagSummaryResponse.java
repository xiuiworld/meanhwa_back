package com.example.meanhwa_back.flower.dto;

import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.domain.TagCategory;
/**
 * 꽃 상세와 큐레이션 스냅샷에서 재사용하는 태그 요약 DTO.
 * 태그 엔티티의 화면 표시값을 안정적인 응답 계약으로 변환한다.
 */
public record TagSummaryResponse(
        Long id,
        TagCategory category,
        String name
) {
    /**
     * 도메인 객체나 스냅샷을 이 API 응답 DTO로 변환한다.
     */
    public static TagSummaryResponse from(Tag tag) {
        return new TagSummaryResponse(tag.getId(), tag.getCategory(), tag.getName());
    }
}
