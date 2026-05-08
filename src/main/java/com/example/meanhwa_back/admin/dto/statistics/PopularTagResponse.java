package com.example.meanhwa_back.admin.dto.statistics;

import com.example.meanhwa_back.tag.domain.TagCategory;

public record PopularTagResponse(
        Long tagId,
        TagCategory category,
        String name,
        long count
) {
}
