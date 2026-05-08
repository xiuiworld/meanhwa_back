package com.example.meanhwa_back.log.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.example.meanhwa_back.common.response.ApiResponse;
import com.example.meanhwa_back.log.domain.ActionType;
import com.example.meanhwa_back.log.dto.CurationResultClickRequest;
import com.example.meanhwa_back.log.service.ActionLogService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/action-logs")
public class ActionLogController {
    private final ActionLogService actionLogService;

    public ActionLogController(ActionLogService actionLogService) {
        this.actionLogService = actionLogService;
    }

    @PostMapping("/curation-result-click")
    public ApiResponse<Void> recordCurationResultClick(@Valid @RequestBody CurationResultClickRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("flowerId", request.flowerId());
        payload.put("tagIds", request.tagIds() == null ? java.util.List.of() : request.tagIds());
        payload.put("rank", request.rank());
        payload.put("score", request.score());
        payload.put("source", request.source());
        actionLogService.record(ActionType.CURATION_RESULT_CLICK, payload);
        return ApiResponse.ok(null);
    }
}
