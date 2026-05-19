package com.example.meanhwa_back.curation.wizard.dto;

import java.util.List;

/**
 * {@code GET /api/v1/curation/steps/{stepKey}/options} 응답.
 */
public record CurationStepOptionsResponse(
        String flowVersion,
        String step,
        int order,
        String questionTitle,
        String questionSubtitle,
        List<CurationWizardOptionDto> options
) {
}
