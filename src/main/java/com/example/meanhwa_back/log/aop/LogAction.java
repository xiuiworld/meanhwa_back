package com.example.meanhwa_back.log.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.meanhwa_back.log.aop.extractor.ActionLogPayloadExtractor;
import com.example.meanhwa_back.log.domain.ActionType;

/**
 * 비즈니스 행동 로그({@code action_logs})를 남길 서비스·컨트롤러 메서드에 붙이는 마커.
 *
 * <p>실제 DB 저장은 {@link ActionLogAspect}가 담당하며, 메서드 본문에
 * {@code actionLogService.record(...)} 를 반복해서 넣지 않아도 된다.
 *
 * <p>payload(JSON) 구성 방식은 {@link #extractor()} 에 지정한
 * {@link ActionLogPayloadExtractor} 구현체가 결정한다.
 *
 * @see ActionLogAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAction {

    /** {@code action_logs.action_type} 에 저장될 이벤트 종류. */
    ActionType value();

    /**
     * 호출 인자·반환값에서 payload Map을 만드는 전략 클래스.
     * <p>Spring Bean으로 등록된 {@link ActionLogPayloadExtractor} 구현체와 동일한 클래스를 지정한다.
     */
    Class<? extends ActionLogPayloadExtractor> extractor();
}
