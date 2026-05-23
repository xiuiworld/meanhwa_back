package com.example.meanhwa_back.admin.dto;

import com.example.meanhwa_back.flower.domain.FlowerTagMapping;
import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.domain.TagCategory;

/**
 * 관리자 꽃 상세에서 특정 꽃에 연결된 태그와 가중치를 표현하는 응답 DTO.
 * 프론트가 매핑 편집 UI를 복원할 수 있도록 태그 식별자와 표시 정보를 함께 담는다.
 */
public record AdminFlowerTagMappingResponse(
        Long id,
        TagCategory category,
        String name,
        int weight
) {
    /**
     * 도메인 객체나 스냅샷을 이 API 응답 DTO로 변환한다.
     */
    public static AdminFlowerTagMappingResponse from(FlowerTagMapping mapping) {
        Tag tag = mapping.getTag();
        return new AdminFlowerTagMappingResponse(
                tag.getId(),
                tag.getCategory(),
                tag.getName(),
                mapping.getWeight()
        );
    }
}
