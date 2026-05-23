package com.example.meanhwa_back.flower.dto;

import java.io.Serializable;

import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.ManagementLevel;
import com.example.meanhwa_back.flower.domain.PriceRange;

/**
 * 꽃 목록과 검색 결과에 사용하는 요약 응답 DTO.
 * 카드 UI에 필요한 핵심 정보만 담아 페이지 응답 크기를 낮춘다.
 */
public record FlowerSummaryResponse(
        Long id,
        String name,
        String imageUrl,
        String coreMeaning,
        String description,
        ManagementLevel managementLevel,
        boolean isPetSafe,
        PriceRange priceRange
) implements Serializable {
    /**
     * 도메인 객체나 스냅샷을 이 API 응답 DTO로 변환한다.
     */
    public static FlowerSummaryResponse from(Flower flower) {
        return new FlowerSummaryResponse(
                flower.getId(),
                flower.getName(),
                flower.getImageUrl(),
                flower.getCoreMeaning(),
                flower.getDescription(),
                flower.getManagementLevel(),
                !flower.isToxicToPets(),
                flower.getPriceRange()
        );
    }
}
