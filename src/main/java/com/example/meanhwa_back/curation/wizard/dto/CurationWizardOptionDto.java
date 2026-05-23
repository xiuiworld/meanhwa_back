package com.example.meanhwa_back.curation.wizard.dto;

/**
 * 큐레이션 플로우 검증과 label snapshot 생성을 위한 단계 선택지 한 항목.
 *
 * @param code    클라이언트·서버 공통 식별자
 * @param label   UI 표시 문구
 */
public record CurationWizardOptionDto(
        String code,
        String label
) {
}
