package com.example.meanhwa_back.admin.dto;

import com.example.meanhwa_back.tag.domain.TagCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminTagRequest(
        @NotNull
        TagCategory category,

        @NotBlank
        @Size(max = 50)
        String name
) {
}
