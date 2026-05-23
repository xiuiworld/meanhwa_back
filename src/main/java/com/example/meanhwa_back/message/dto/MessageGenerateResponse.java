package com.example.meanhwa_back.message.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.meanhwa_back.flower.dto.TagSummaryResponse;
/**
 * 메시지 생성 결과와 저장된 메시지 조회에 공통으로 쓰는 응답 DTO.
 * 생성된 문구와 꽃, 태그, 큐레이션 결과 연결 정보를 함께 전달한다.
 */

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
