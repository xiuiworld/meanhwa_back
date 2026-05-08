package com.example.meanhwa_back.admin.dto;

import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.domain.TagCategory;

public record AdminTagResponse(
        Long id,
        TagCategory category,
        String name
) {
    public static AdminTagResponse from(Tag tag) {
        return new AdminTagResponse(tag.getId(), tag.getCategory(), tag.getName());
    }
}
