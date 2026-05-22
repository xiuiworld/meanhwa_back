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

        @Size(max = 150)
        String scientificName,

        @Size(max = 100)
        String origin,

        @Size(max = 100)
        String bloomingSeason,

        @Size(max = 100)
        String scent,

        @NotNull
        ManagementLevel managementLevel,

        String managementInfo,

        boolean isToxicToPets,

        @NotNull
        PriceRange priceRange
) {
}
