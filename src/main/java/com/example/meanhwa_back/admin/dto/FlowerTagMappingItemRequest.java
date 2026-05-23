package com.example.meanhwa_back.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
/**
 * 꽃 하나에 연결할 태그와 추천 가중치 한 건을 표현하는 요청 항목.
 * 가중치는 추천 점수 계산에 직접 쓰이므로 허용 범위를 1부터 5까지로 제한한다.
 */
public record FlowerTagMappingItemRequest(
        @NotNull
        Long tagId,

        @Min(1)
        @Max(5)
        int weight
) {
}
