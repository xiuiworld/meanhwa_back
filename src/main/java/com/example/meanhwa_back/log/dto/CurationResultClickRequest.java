package com.example.meanhwa_back.log.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CurationResultClickRequest(
        @NotNull Long flowerId,
        List<Long> tagIds,
        @Min(1) Integer rank,
        Integer score,
        String source
) {
}
