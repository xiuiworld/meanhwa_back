package com.example.meanhwa_back.tag.dto;

import java.io.Serializable;
import java.util.List;

import com.example.meanhwa_back.tag.domain.TagCategory;

public record TagCategoryResponse(
        TagCategory category,
        List<TagItemResponse> tags
) implements Serializable {
}
