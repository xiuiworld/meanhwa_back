package com.example.meanhwa_back.message.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageGenerateRequest(
        @NotNull Long flowerId,
        List<Long> selectedTagIds,
        Long curationResultId,
        @NotBlank String senderName,
        @NotBlank String receiverName
) {
}
