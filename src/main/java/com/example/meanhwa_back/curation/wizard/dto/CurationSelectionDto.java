package com.example.meanhwa_back.curation.wizard.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 위저드 단계별 사용자 선택.
 *
 * @param step {@link com.example.meanhwa_back.curation.wizard.domain.CurationStepKey} 이름
 * @param code 안정 식별자 (예: {@code BIRTHDAY}, {@code LOVE_3})
 */
public record CurationSelectionDto(
        @NotBlank String step,
        @NotBlank String code
) {
}
