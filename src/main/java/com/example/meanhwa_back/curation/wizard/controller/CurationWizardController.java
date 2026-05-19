package com.example.meanhwa_back.curation.wizard.controller;

import com.example.meanhwa_back.common.response.ApiResponse;
import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.curation.dto.CurationFlowerResponse;
import com.example.meanhwa_back.curation.wizard.dto.CurationFlowResponse;
import com.example.meanhwa_back.curation.wizard.dto.CurationStepOptionsResponse;
import com.example.meanhwa_back.curation.wizard.dto.CurationWizardResultsRequest;
import com.example.meanhwa_back.curation.wizard.service.CurationWizardService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 분기형 큐레이션 위저드 API (v2).
 * <p>레거시 {@code GET /api/v1/curation?tagIds=} 는 유지되며, 본 컨트롤러가 신규 6단계 플로우를 담당한다.
 */
@RestController
@RequestMapping("/api/v1/curation")
public class CurationWizardController {
    private final CurationWizardService curationWizardService;

    public CurationWizardController(CurationWizardService curationWizardService) {
        this.curationWizardService = curationWizardService;
    }

    /** P1: 플로우 메타(버전, 단계 정의, dependsOn). */
    @GetMapping("/flow")
    public ApiResponse<CurationFlowResponse> getFlow(
            @RequestParam(required = false) String flowVersion
    ) {
        return ApiResponse.ok(curationWizardService.getFlow(flowVersion));
    }

    /** P1: 분기 반영 선택지 + 동적 질문 문구. {@code selections}는 URL-encoded JSON 배열. */
    @GetMapping("/steps/{stepKey}/options")
    public ApiResponse<CurationStepOptionsResponse> getStepOptions(
            @PathVariable String stepKey,
            @RequestParam(required = false) String flowVersion,
            @RequestParam(required = false) String selections
    ) {
        return ApiResponse.ok(curationWizardService.getStepOptions(stepKey, flowVersion, selections));
    }

    /** P2: 6단계 선택 → code→tagId 변환 후 점수 합산·정렬. */
    @PostMapping("/results")
    public ApiResponse<PageResponse<CurationFlowerResponse>> getResults(
            @Valid @RequestBody CurationWizardResultsRequest request
    ) {
        return ApiResponse.ok(curationWizardService.getResults(request));
    }
}
