package com.example.meanhwa_back.log.controller;

import com.example.meanhwa_back.common.response.ApiResponse;
import com.example.meanhwa_back.log.aop.LogAction;
import com.example.meanhwa_back.log.aop.extractor.CurationResultClickPayloadExtractor;
import com.example.meanhwa_back.log.domain.ActionType;
import com.example.meanhwa_back.log.dto.CurationResultClickRequest;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 클라이언트에서 전송하는 행동 로그 수집 API.
 *
 * <p>서버 내부 이벤트는 {@link LogAction} AOP로 수집하고,
 * 프론트가 결과 카드 클릭 시점을 알려줄 때만 본 컨트롤러를 사용한다.
 */
@RestController
@RequestMapping("/api/v1/action-logs")
public class ActionLogController {

    /**
     * 큐레이션 결과 꽃 카드 클릭 (프론트 전송).
     *
     * <p>위저드 v2: {@code source=curation-v2}, {@code flowVersion}, 6단계 {@code selections} 권장 (P3 §9).
     * 목록 조회 로그는 {@code POST /curation/results} AOP({@code CURATION_START})가 별도로 남긴다.
     */
    @LogAction(value = ActionType.CURATION_RESULT_CLICK, extractor = CurationResultClickPayloadExtractor.class)
    @PostMapping("/curation-result-click")
    public ApiResponse<Void> recordCurationResultClick(@Valid @RequestBody CurationResultClickRequest request) {
        return ApiResponse.ok(null);
    }
}
