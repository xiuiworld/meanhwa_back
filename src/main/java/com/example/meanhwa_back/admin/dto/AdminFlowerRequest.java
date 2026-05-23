package com.example.meanhwa_back.admin.dto;

import com.example.meanhwa_back.flower.domain.ManagementLevel;
import com.example.meanhwa_back.flower.domain.PriceRange;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
