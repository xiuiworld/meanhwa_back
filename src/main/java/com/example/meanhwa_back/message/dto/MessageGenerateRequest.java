package com.example.meanhwa_back.message.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 선물 메시지 생성 요청. 서버는 유효한 요청만 사용자별 생성 quota에 반영한다. */
public record MessageGenerateRequest(
        @NotNull @Positive Long flowerId,
        @Size(max = 20) List<@Positive Long> selectedTagIds,
        @Positive Long curationResultId,
        @NotBlank @Size(max = 30) String senderName,
        @NotBlank @Size(max = 30) String receiverName
) {
}
