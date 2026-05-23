package com.example.meanhwa_back.admin.dto;

import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.domain.TagCategory;
/**
 * 관리자 태그 목록과 상세 화면에서 사용하는 태그 응답 DTO.
 * 도메인 엔티티를 직접 노출하지 않고 화면에 필요한 식별자, 카테고리, 이름만 전달한다.
 */
public record AdminTagResponse(
        Long id,
        TagCategory category,
        String name
) {
    /**
     * 도메인 객체나 스냅샷을 이 API 응답 DTO로 변환한다.
     */
    public static AdminTagResponse from(Tag tag) {
        return new AdminTagResponse(tag.getId(), tag.getCategory(), tag.getName());
    }
}
