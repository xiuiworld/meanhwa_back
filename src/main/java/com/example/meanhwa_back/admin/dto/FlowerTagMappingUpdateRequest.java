package com.example.meanhwa_back.admin.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
/**
 * 관리자 꽃-태그 매핑 일괄 교체 요청.
 * 서비스 계층은 이 목록을 기준으로 기존 매핑을 지우고 새 매핑을 저장한다.
 */
public record FlowerTagMappingUpdateRequest(
        @NotNull
        List<@NotNull @Valid FlowerTagMappingItemRequest> tags
) {
}
