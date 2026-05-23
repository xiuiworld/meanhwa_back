package com.example.meanhwa_back.curation.wizard.controller;

import com.example.meanhwa_back.common.response.ApiResponse;
import com.example.meanhwa_back.curation.wizard.dto.CurationResultsResponse;
import com.example.meanhwa_back.curation.wizard.dto.CurationWizardResultsRequest;
import com.example.meanhwa_back.curation.wizard.service.CurationWizardService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 분기형 큐레이션 위저드 결과 API. */
@RestController
@RequestMapping("/api/v1/curation")
public class CurationWizardController {
    private final CurationWizardService curationWizardService;

    public CurationWizardController(CurationWizardService curationWizardService) {
        this.curationWizardService = curationWizardService;
    }

    /** 6단계 선택 → code→tagId 변환 후 점수 합산·정렬. */
    @PostMapping("/results")
    public ApiResponse<CurationResultsResponse> getResults(
            @Valid @RequestBody CurationWizardResultsRequest request
    ) {
        return ApiResponse.ok(curationWizardService.getResults(request));
    }
}
