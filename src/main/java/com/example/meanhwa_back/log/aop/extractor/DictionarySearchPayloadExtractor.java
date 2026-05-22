package com.example.meanhwa_back.log.aop.extractor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.flower.dto.FlowerSummaryResponse;
import com.example.meanhwa_back.log.aop.LogAction;

import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

/**
 * {@code FlowerService#searchFlowers} 용 payload.
 *
 * <p>검색어가 비어 있으면 사전 <em>전체 목록</em> 조회로 보고 로그를 남기지 않는다.
 * (기존 수동 {@code if (normalizedKeyword != null)} 동작과 동일)
 */
@Component
public class DictionarySearchPayloadExtractor implements ActionLogPayloadExtractor {

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> extract(JoinPoint joinPoint, Object returnValue, LogAction logAction) {
        Object[] args = joinPoint.getArgs();
        String keyword = (String) args[0];
        int page = (int) args[args.length >= 7 ? 5 : 1];
        int size = (int) args[args.length >= 7 ? 6 : 2];

        if (keyword == null || keyword.isBlank()) {
            return Optional.empty();
        }

        PageResponse<FlowerSummaryResponse> response = (PageResponse<FlowerSummaryResponse>) returnValue;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("keyword", keyword.trim());
        payload.put("page", page);
        payload.put("size", size);
        payload.put("resultCount", response.content().size());
        payload.put("totalElements", response.totalElements());
        return Optional.of(payload);
    }
}
