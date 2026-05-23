package com.example.meanhwa_back.tag.dto;

import java.io.Serializable;
import java.util.List;

import com.example.meanhwa_back.tag.domain.TagCategory;

/**
 * 태그 목록 API에서 카테고리별 태그 묶음을 표현하는 응답 DTO.
 * 프론트가 큐레이션 필터 UI를 바로 구성할 수 있도록 카테고리 이름과 항목 목록을 함께 담는다.
 */
public record TagCategoryResponse(
        TagCategory category,
        List<TagItemResponse> tags
) implements Serializable {
}
