package com.example.meanhwa_back.flower.dto;

import java.util.List;

import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.ManagementLevel;
import com.example.meanhwa_back.flower.domain.PriceRange;
/**
 * 꽃 상세 조회 응답 DTO.
 * 목록보다 상세한 설명과 연결 태그를 포함해 상세 화면 렌더링에 필요한 값을 제공한다.
 */
public record FlowerDetailResponse(
        Long id,
        String name,
        String imageUrl,
        String coreMeaning,
        String description,
        ManagementLevel managementLevel,
        boolean isPetSafe,
        PriceRange priceRange,
        List<TagSummaryResponse> tags
) {
    /**
     * 도메인 값들을 조합해 이 API 응답 DTO를 만든다.
     */
    public static FlowerDetailResponse of(Flower flower, List<TagSummaryResponse> tags) {
        return new FlowerDetailResponse(
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
