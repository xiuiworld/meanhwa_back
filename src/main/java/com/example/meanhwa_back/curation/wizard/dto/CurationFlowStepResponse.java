package com.example.meanhwa_back.curation.wizard.dto;

import java.util.List;

public record CurationFlowStepResponse(
        String key,
        int order,
        String defaultQuestionTitle,
        String defaultQuestionSubtitle,
        String selectionMode,
        List<String> dependsOn
) {
}
