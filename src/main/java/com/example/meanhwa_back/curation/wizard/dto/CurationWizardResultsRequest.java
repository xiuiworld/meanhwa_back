package com.example.meanhwa_back.curation.wizard.dto;

import java.util.List;

import com.example.meanhwa_back.common.web.PageRequestUtils;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * {@code POST /api/v1/curation/results} 요청.
 */
public record CurationWizardResultsRequest(
        String flowVersion,
        @NotNull @NotEmpty @Valid List<CurationSelectionDto> selections,
        Integer page,
        Integer size
) {
    /** JSON에 page가 없으면 0. */
    public int resolvedPage() {
        return page == null ? 0 : PageRequestUtils.normalizePage(page);
    }

    /** JSON에 size가 없으면 20. */
    public int resolvedSize() {
        return size == null ? PageRequestUtils.DEFAULT_SIZE : PageRequestUtils.normalizeSize(size);
    }
}
