package com.example.meanhwa_back.tag.dto;

import java.io.Serializable;

import com.example.meanhwa_back.tag.domain.Tag;

public record TagItemResponse(
        Long id,
        String name
) implements Serializable {
    public static TagItemResponse from(Tag tag) {
        return new TagItemResponse(tag.getId(), tag.getName());
    }
}
