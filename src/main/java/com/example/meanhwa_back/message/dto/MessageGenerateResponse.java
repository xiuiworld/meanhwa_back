package com.example.meanhwa_back.message.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.meanhwa_back.flower.dto.TagSummaryResponse;

public record MessageGenerateResponse(
        Long flowerId,
        Long id,
        String flowerName,
        String flowerImageUrl,
        String coreMeaning,
        List<TagSummaryResponse> selectedTags,
        Long curationResultId,
        String senderName,
        String receiverName,
        LocalDateTime createdAt,
        String message
) {
}
