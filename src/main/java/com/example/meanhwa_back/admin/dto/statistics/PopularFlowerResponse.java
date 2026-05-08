package com.example.meanhwa_back.admin.dto.statistics;

public record PopularFlowerResponse(
        Long flowerId,
        String name,
        String imageUrl,
        long count
) {
}
