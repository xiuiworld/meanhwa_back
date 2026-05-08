package com.example.meanhwa_back.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FlowerTagMappingItemRequest(
        @NotNull
        Long tagId,

        @Min(1)
        @Max(5)
        int weight
) {
}
