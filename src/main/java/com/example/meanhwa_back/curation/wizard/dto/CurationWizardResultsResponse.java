package com.example.meanhwa_back.curation.wizard.dto;

import java.io.Serializable;
import java.util.List;

import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.curation.dto.CurationFlowerResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * {@code POST /api/v1/curation/results} 응답.
 *
 * <p>기존 {@link PageResponse} 필드({@code content}, {@code page}, …)는 그대로 두고,
 * 로그인 사용자의 이력 저장에 성공한 경우에만 {@code curationResultId}를 추가한다.
 * 비로그인·저장 실패 시 {@code null}이라 JSON에서 필드가 생략된다.
 *
 * <p>프론트는 위저드 직후 {@code POST /messages/generate}의 {@code curationResultId}에
 * 이 값을 넘겨 {@code GET .../curation-results/latest} 추가 호출을 줄일 수 있다.
 *
 * @see com.example.meanhwa_back.curation.wizard.service.CurationWizardService#getResults
 */
public record CurationWizardResultsResponse(
        /** 저장된 {@code user_curation_results.id}. {@code latest}/{@code {resultId}} 조회의 id와 동일. */
        @JsonInclude(JsonInclude.Include.NON_NULL) Long curationResultId,
        List<CurationFlowerResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) implements Serializable {
    public static CurationWizardResultsResponse from(
            PageResponse<CurationFlowerResponse> page,
            Long curationResultId
    ) {
        return new CurationWizardResultsResponse(
                curationResultId,
                page.content(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.hasNext()
        );
    }
}
