package com.example.meanhwa_back.log.aop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.meanhwa_back.log.aop.extractor.ActionLogPayloadExtractor;

import org.springframework.stereotype.Component;

/**
 * {@link LogAction#extractor()} 에 적힌 클래스 → Spring Bean 매핑 테이블.
 *
 * <p>Aspect는 매 요청마다 extractor 구현체를 new 하지 않고, 여기서 등록된 싱글톤을 꺼내 쓴다.
 */
@Component
public class ActionLogPayloadExtractorRegistry {
    private final Map<Class<? extends ActionLogPayloadExtractor>, ActionLogPayloadExtractor> extractorsByClass;

    public ActionLogPayloadExtractorRegistry(List<ActionLogPayloadExtractor> extractors) {
        Map<Class<? extends ActionLogPayloadExtractor>, ActionLogPayloadExtractor> map = new HashMap<>();
        for (ActionLogPayloadExtractor extractor : extractors) {
            map.put(extractor.getClass(), extractor);
        }
        this.extractorsByClass = Map.copyOf(map);
    }
/**
 * 지정한 payload extractor bean이 등록되어 있는지 확인하고 반환한다.
 */

    public ActionLogPayloadExtractor require(Class<? extends ActionLogPayloadExtractor> type) {
        ActionLogPayloadExtractor extractor = extractorsByClass.get(type);
        if (extractor == null) {
            throw new IllegalStateException(
                    "ActionLogPayloadExtractor bean not registered: " + type.getName()
                            + ". @Component 및 List 주입 대상인지 확인하세요."
            );
        }
        return extractor;
    }
}
