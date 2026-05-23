package com.example.meanhwa_back.log.aop.extractor;

import java.util.Map;
import java.util.Optional;

import com.example.meanhwa_back.log.aop.LogAction;

import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

/**
 * {@code FlowerService#getFlower} 용 payload.
 * <p>관리자 통계·인기 식물 집계에서 {@code flowerId} 로 조인한다.
 */
@Component
public class FlowerDetailViewPayloadExtractor implements ActionLogPayloadExtractor {

    /**
     * 컨트롤러 호출 인자를 행동 로그 actionData JSON으로 변환한다.
     */
    @Override
    public Optional<Map<String, Object>> extract(JoinPoint joinPoint, Object returnValue, LogAction logAction) {
        Long flowerId = (Long) joinPoint.getArgs()[0];
        return Optional.of(Map.of("flowerId", flowerId));
    }
}
