package com.example.meanhwa_back.admin.dto;

import com.example.meanhwa_back.flower.domain.FlowerTagMapping;
import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.domain.TagCategory;

public record AdminFlowerTagMappingResponse(
        Long id,
        TagCategory category,
        String name,
        int weight
) {
    public static AdminFlowerTagMappingResponse from(FlowerTagMapping mapping) {
        Tag tag = mapping.getTag();
        return new AdminFlowerTagMappingResponse(
                tag.getId(),
                tag.getCategory(),
                tag.getName(),
                mapping.getWeight()
        );
    }
}
