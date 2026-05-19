package com.example.meanhwa_back.log.aop.extractor;

import java.util.Map;
import java.util.Optional;

import com.example.meanhwa_back.log.aop.LogAction;

import org.aspectj.lang.JoinPoint;

/**
 * {@link LogAction} 이 붙은 메서드 한 번의 호출에서 {@code action_logs.action_data} JSON payload를 만든다.
 *
 * <p>{@link Optional#empty()} 를 반환하면 해당 호출은 로그를 남기지 않는다.
 * (예: 검색어 없이 사전 목록만 조회한 경우)
 */
public interface ActionLogPayloadExtractor {

    /**
     * @param joinPoint   AOP가 가로챈 메서드의 인자·시그니처 정보
     * @param returnValue {@code @AfterReturning} 시점의 반환값 (void API면 {@code null})
     * @param logAction   대상 메서드에 선언된 {@link LogAction} 어노테이션
     * @return 저장할 payload. 비어 있으면 로그 생략
     */
    Optional<Map<String, Object>> extract(JoinPoint joinPoint, Object returnValue, LogAction logAction);
}
