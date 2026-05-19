package com.example.meanhwa_back.curation.controller;

import java.util.List;

import com.example.meanhwa_back.common.response.ApiResponse;
import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.curation.dto.CurationFlowerResponse;
import com.example.meanhwa_back.curation.service.CurationService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 태그·필터 기반 꽃 큐레이션 API. */
@RestController
@RequestMapping("/api/v1/curation")
public class CurationController {
    private final CurationService curationService;

    public CurationController(CurationService curationService) {
        this.curationService = curationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CurationFlowerResponse>> curate(
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) Boolean isPetSafe,
            @RequestParam(required = false) String priceRange,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(curationService.curate(tagIds, isPetSafe, priceRange, page, size));
    }
}
