package com.example.meanhwa_back.log.aop.extractor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.meanhwa_back.curation.wizard.dto.CurationSelectionDto;
import com.example.meanhwa_back.log.aop.LogAction;
import com.example.meanhwa_back.log.dto.CurationResultClickRequest;

import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

/**
 * {@code POST /api/v1/action-logs/curation-result-click} → {@code action_logs.action_data} 변환.
 *
 * <p>클릭 시점의 {@code flowerId}, {@code rank} 와 함께
 * 위저드 {@link CurationResultClickRequest#selections()} 스냅샷을 남기면
 * “어떤 분기 경로에서 이 꽃을 눌렀는지” 분석할 수 있다.
 */
@Component
public class CurationResultClickPayloadExtractor implements ActionLogPayloadExtractor {

    /**
     * 컨트롤러 호출 인자를 행동 로그 actionData JSON으로 변환한다.
     */
    @Override
    public Optional<Map<String, Object>> extract(JoinPoint joinPoint, Object returnValue, LogAction logAction) {
        CurationResultClickRequest request = (CurationResultClickRequest) joinPoint.getArgs()[0];

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("flowerId", request.flowerId());
        payload.put("tagIds", request.tagIds() == null ? List.of() : request.tagIds());
        payload.put("rank", request.rank());
        payload.put("score", request.score());
        payload.put("source", request.source());
        payload.put("flowVersion", request.flowVersion());
        payload.put("selections", toSelectionMaps(request.selections()));
        return Optional.of(payload);
    }

    /** null/빈 목록은 {@code []} 로 저장해 JSON 키를 일정하게 유지한다. */
    private static List<Map<String, String>> toSelectionMaps(List<CurationSelectionDto> selections) {
        if (selections == null || selections.isEmpty()) {
            return List.of();
        }
        return selections.stream()
                .map(s -> Map.of("step", s.step(), "code", s.code()))
                .toList();
    }
}
