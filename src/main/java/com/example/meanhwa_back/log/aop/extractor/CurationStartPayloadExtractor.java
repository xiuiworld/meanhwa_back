package com.example.meanhwa_back.log.aop.extractor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.curation.dto.CurationFlowerResponse;
import com.example.meanhwa_back.flower.domain.PriceRange;
import com.example.meanhwa_back.log.aop.LogAction;

import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

/**
 * 레거시 {@code GET /api/v1/curation} → {@code CurationService#curate} 용 payload.
 *
 * <p>필드 구성은 리팩터 전 {@code recordCurationLog} 와 동일하게 유지해
 * 기존 통합 테스트·통계 쿼리가 깨지지 않도록 한다.
 */
@Component
public class CurationStartPayloadExtractor implements ActionLogPayloadExtractor {

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> extract(JoinPoint joinPoint, Object returnValue, LogAction logAction) {
        Object[] args = joinPoint.getArgs();
        List<Long> tagIds = normalizeTagIds((List<Long>) args[0]);
        Boolean isPetSafe = (Boolean) args[1];
        String priceRangeValue = (String) args[2];
        int page = (int) args[3];
        int size = (int) args[4];

        PriceRange priceRange = PriceRange.from(priceRangeValue);
        PageResponse<CurationFlowerResponse> response = (PageResponse<CurationFlowerResponse>) returnValue;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tagIds", tagIds);
        payload.put("isPetSafe", isPetSafe);
        payload.put("priceRange", priceRange == null ? null : priceRange.name());
        payload.put("page", page);
        payload.put("size", size);
        payload.put("resultCount", response.content().size());
        payload.put("totalElements", response.totalElements());
        payload.put("resultFlowerIds", response.content().stream()
                .map(CurationFlowerResponse::flowerId)
                .toList());
        payload.put("source", "curation-legacy");
        return Optional.of(payload);
    }

    private static List<Long> normalizeTagIds(List<Long> tagIds) {
        if (tagIds == null) {
            return List.of();
        }
        return tagIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
    }
}
