package com.example.meanhwa_back.flower.controller;

import com.example.meanhwa_back.common.response.ApiResponse;
import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.flower.dto.FlowerDetailResponse;
import com.example.meanhwa_back.flower.dto.FlowerSummaryResponse;
import com.example.meanhwa_back.flower.service.FlowerService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/flowers")
public class FlowerController {
    private final FlowerService flowerService;

    public FlowerController(FlowerService flowerService) {
        this.flowerService = flowerService;
    }

    @GetMapping
    public ApiResponse<PageResponse<FlowerSummaryResponse>> getFlowers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(flowerService.searchFlowers(keyword, page, size));
    }

    @GetMapping("/{flowerId}")
    public ApiResponse<FlowerDetailResponse> getFlower(@PathVariable Long flowerId) {
        return ApiResponse.ok(flowerService.getFlower(flowerId));
    }
}
