package com.example.meanhwa_back.curation.wizard.dto;

import java.util.List;

/**
 * {@code GET /api/v1/curation/flow} 응답.
 */
public record CurationFlowResponse(
        String flowVersion,
        int totalSteps,
        List<CurationFlowStepResponse> steps
) {
}
