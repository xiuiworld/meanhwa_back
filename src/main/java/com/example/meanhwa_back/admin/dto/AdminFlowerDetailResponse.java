package com.example.meanhwa_back.admin.dto;

import java.util.List;

import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.ManagementLevel;
import com.example.meanhwa_back.flower.domain.PriceRange;
/**
 * 관리자 꽃 상세 화면에 필요한 꽃 본문과 태그 매핑을 함께 내려주는 응답 DTO.
 * 목록 응답보다 많은 필드를 포함하므로 백오피스 수정 화면의 원본 데이터로 사용된다.
 */
public record AdminFlowerDetailResponse(
        Long id,
        String name,
        String imageUrl,
        String coreMeaning,
        String description,
        ManagementLevel managementLevel,
        boolean isPetSafe,
        PriceRange priceRange,
        List<AdminFlowerTagMappingResponse> tags
) {
    /**
     * 도메인 값들을 조합해 이 API 응답 DTO를 만든다.
     */
    public static AdminFlowerDetailResponse of(Flower flower, List<AdminFlowerTagMappingResponse> tags) {
        return new AdminFlowerDetailResponse(
                flower.getId(),
                flower.getName(),
                flower.getImageUrl(),
                flower.getCoreMeaning(),
                flower.getDescription(),
                flower.getManagementLevel(),
                !flower.isToxicToPets(),
                flower.getPriceRange(),
                tags
        );
    }
}
