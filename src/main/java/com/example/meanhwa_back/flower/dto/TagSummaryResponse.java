package com.example.meanhwa_back.flower.dto;

import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.domain.TagCategory;

public record TagSummaryResponse(
        Long id,
        TagCategory category,
        String name
) {
    public static TagSummaryResponse from(Tag tag) {
        return new TagSummaryResponse(tag.getId(), tag.getCategory(), tag.getName());
    }
}
