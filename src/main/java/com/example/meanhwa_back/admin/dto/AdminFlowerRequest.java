package com.example.meanhwa_back.admin.dto;

import com.example.meanhwa_back.flower.domain.ManagementLevel;
import com.example.meanhwa_back.flower.domain.PriceRange;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 관리자 꽃 생성과 수정 요청 본문.
 * 운영 데이터 품질을 위해 이름, 의미, 가격대, 관리 난이도 같은 필수 속성을 Bean Validation으로 제한한다.
 */
public record AdminFlowerRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 255)
        String imageUrl,

        @Size(max = 100)
        String coreMeaning,

        String description,

        @NotNull
        ManagementLevel managementLevel,

        boolean isToxicToPets,

        @NotNull
        PriceRange priceRange
) {
}
