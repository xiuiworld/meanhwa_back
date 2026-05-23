package com.example.meanhwa_back.curation.history.controller;

import com.example.meanhwa_back.common.response.ApiResponse;
import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.curation.history.dto.CurationResultDetailResponse;
import com.example.meanhwa_back.curation.history.dto.CurationResultSummaryResponse;
import com.example.meanhwa_back.curation.history.service.CurationResultHistoryService;

import jakarta.validation.constraints.Positive;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 사용자의 큐레이션 결과 이력 API. */
@RestController
@RequestMapping("/api/v1/users/me/curation-results")
@Validated
public class CurationResultHistoryController {
    private final CurationResultHistoryService curationResultHistoryService;

    public CurationResultHistoryController(CurationResultHistoryService curationResultHistoryService) {
        this.curationResultHistoryService = curationResultHistoryService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CurationResultSummaryResponse>> getResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(curationResultHistoryService.getMyResults(page, size));
    }

    @GetMapping("/latest")
    public ApiResponse<CurationResultDetailResponse> getLatestResult() {
        return ApiResponse.ok(curationResultHistoryService.getLatestResult());
    }

    @GetMapping("/{resultId}")
    public ApiResponse<CurationResultDetailResponse> getResult(@PathVariable @Positive Long resultId) {
        return ApiResponse.ok(curationResultHistoryService.getResult(resultId));
    }
}
