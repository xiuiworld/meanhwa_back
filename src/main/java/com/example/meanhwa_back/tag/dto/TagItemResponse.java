package com.example.meanhwa_back.tag.dto;

import java.io.Serializable;

import com.example.meanhwa_back.tag.domain.Tag;
/**
 * 태그 목록의 개별 태그 항목 응답 DTO.
 * 식별자, 코드, 이름을 제공해 검색 필터와 큐레이션 선택값에 재사용된다.
 */

public record TagItemResponse(
        Long id,
        String name
) implements Serializable {
    public static TagItemResponse from(Tag tag) {
        return new TagItemResponse(tag.getId(), tag.getName());
    }
}
