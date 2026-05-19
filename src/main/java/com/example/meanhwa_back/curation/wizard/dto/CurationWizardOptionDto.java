package com.example.meanhwa_back.curation.wizard.dto;

/**
 * 단계 선택지 한 항목.
 *
 * @param code    클라이언트·서버 공통 식별자
 * @param label   UI 표시 문구
 * @param tagId   P2: {@code tags.id}. DB에 code가 없으면 null
 * @param description 보조 설명 (선택)
 */
public record CurationWizardOptionDto(
        String code,
        String label,
        Long tagId,
        String description
) {
    public CurationWizardOptionDto withTagId(Long resolvedTagId) {
        return new CurationWizardOptionDto(code, label, resolvedTagId, description);
    }
}
