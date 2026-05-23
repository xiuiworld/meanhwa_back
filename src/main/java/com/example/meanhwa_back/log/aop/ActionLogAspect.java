package com.example.meanhwa_back.log.aop;

import java.util.Map;
import java.util.Optional;

import com.example.meanhwa_back.log.aop.extractor.ActionLogPayloadExtractor;
import com.example.meanhwa_back.log.service.ActionLogService;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * {@link LogAction} 이 선언된 메서드가 <strong>정상 종료</strong>한 뒤 {@code action_logs} 에 행동 로그를 남긴다.
 *
 * <h2>왜 AOP인가</h2>
 * <ul>
 * <li>서비스 본문에서 {@link ActionLogService#record} 호출을 제거해 도메인 코드가 짧아진다.</li>
 * <li>새 API 추가 시 어노테이션만 붙이면 되어 로그 누락을 줄인다.</li>
 * <li>payload 형식은 {@link ActionLogPayloadExtractor} 로 모아 통계·집계 시 필드가 들쭉날쭉하지 않게 한다.</li>
 * </ul>
 *
 * <h2>동작 시점</h2>
 * {@link AfterReturning} 이므로 예외로 실패한 요청(4xx/5xx)은 기록하지 않는다.
 * 기존 수동 {@code record()} 호출과 동일한 의미다.
 *
 * <h2>운영 로그와의 구분</h2>
 * SLF4J·CloudWatch 같은 <em>장애/지연 추적</em> 로그와는 별개이며,
 * 관리자 통계({@code AdminStatisticsService})가 읽는 <em>비즈니스 이벤트</em> 전용이다.
 *
 * @see LogAction
 * @see ActionLogService
 */
@Aspect
@Component
public class ActionLogAspect {
    private static final Logger log = LoggerFactory.getLogger(ActionLogAspect.class);

    private final ActionLogService actionLogService;
    private final ActionLogPayloadExtractorRegistry extractorRegistry;

    public ActionLogAspect(
            ActionLogService actionLogService,
            ActionLogPayloadExtractorRegistry extractorRegistry
    ) {
        this.actionLogService = actionLogService;
        this.extractorRegistry = extractorRegistry;
    }

    /**
     * {@code @LogAction} 이 붙은 모든 메서드의 성공 반환 직후에 실행된다.
     * pointcut 표현식 {@code @annotation(logAction)} 은 메서드에 붙은 어노테이션 인스턴스를 인자로 넘겨준다.
     */
    @AfterReturning(pointcut = "@annotation(logAction)", returning = "returnValue")
    public void afterSuccessfulInvocation(JoinPoint joinPoint, LogAction logAction, Object returnValue) {
        try {
            ActionLogPayloadExtractor extractor = extractorRegistry.require(logAction.extractor());
            Optional<Map<String, Object>> payload = extractor.extract(joinPoint, returnValue, logAction);
            if (payload.isEmpty()) {
                return;
            }
            actionLogService.record(logAction.value(), payload.get());
        } catch (Exception exception) {
            // ActionLogService 와 동일하게: 로그 실패가 사용자 API 를 깨지 않도록 삼킨다.
            String method = resolveMethodName(joinPoint);
            log.warn(
                    "Failed to record action log via AOP. actionType={}, method={}",
                    logAction.value(),
                    method,
                    exception
            );
        }
    }

    private static String resolveMethodName(JoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            return methodSignature.getDeclaringType().getSimpleName()
                    + "#"
                    + methodSignature.getName();
        }
        return joinPoint.getSignature().toShortString();
    }
}
