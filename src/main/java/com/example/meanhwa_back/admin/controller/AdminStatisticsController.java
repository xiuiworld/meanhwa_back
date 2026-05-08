package com.example.meanhwa_back.admin.controller;

import java.time.LocalDate;
import java.util.List;

import com.example.meanhwa_back.admin.dto.statistics.DailyActiveUserResponse;
import com.example.meanhwa_back.admin.dto.statistics.PopularFlowerResponse;
import com.example.meanhwa_back.admin.dto.statistics.PopularTagResponse;
import com.example.meanhwa_back.admin.dto.statistics.StatisticsSummaryResponse;
import com.example.meanhwa_back.admin.service.AdminStatisticsService;
import com.example.meanhwa_back.common.response.ApiResponse;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/statistics")
public class AdminStatisticsController {
    private final AdminStatisticsService adminStatisticsService;

    public AdminStatisticsController(AdminStatisticsService adminStatisticsService) {
        this.adminStatisticsService = adminStatisticsService;
    }

    @GetMapping("/summary")
    public ApiResponse<StatisticsSummaryResponse> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.ok(adminStatisticsService.getSummary(from, to));
    }

    @GetMapping("/popular-tags")
    public ApiResponse<List<PopularTagResponse>> getPopularTags(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.ok(adminStatisticsService.getPopularTags(
                from,
                to,
                adminStatisticsService.limitOrDefault(limit)
        ));
    }

    @GetMapping("/popular-flowers")
    public ApiResponse<List<PopularFlowerResponse>> getPopularFlowers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.ok(adminStatisticsService.getPopularFlowers(
                from,
                to,
                adminStatisticsService.limitOrDefault(limit)
        ));
    }

    @GetMapping("/daily-active-users")
    public ApiResponse<List<DailyActiveUserResponse>> getDailyActiveUsers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.ok(adminStatisticsService.getDailyActiveUsers(from, to));
    }
}
