package com.example.meanhwa_back.log.aop;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.flower.dto.FlowerSummaryResponse;
import com.example.meanhwa_back.log.aop.extractor.DictionarySearchPayloadExtractor;

import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
/**
 * ActionLog AOP payload extractor들의 JSON 변환 규칙을 검증하는 테스트.
 * 컨트롤러 요청이 통계용 actionData로 안정적으로 추출되는지 확인한다.
 */

@ExtendWith(MockitoExtension.class)
class ActionLogPayloadExtractorTest {

    private final DictionarySearchPayloadExtractor extractor = new DictionarySearchPayloadExtractor();

    @Test
    void dictionarySearchSkipsBlankKeyword() {
        JoinPoint joinPoint = Mockito.mock(JoinPoint.class);
        Mockito.when(joinPoint.getArgs()).thenReturn(new Object[] { "   ", 0, 20 });

        Optional<Map<String, Object>> payload = extractor.extract(
                joinPoint,
                PageResponse.of(List.<FlowerSummaryResponse>of(), 0, 20, 0),
                null
        );

        assertThat(payload).isEmpty();
    }

    @Test
    void dictionarySearchBuildsPayloadForKeyword() {
        JoinPoint joinPoint = Mockito.mock(JoinPoint.class);
        Mockito.when(joinPoint.getArgs()).thenReturn(new Object[] { " 장미 ", 0, 20 });

        Optional<Map<String, Object>> payload = extractor.extract(
                joinPoint,
                PageResponse.of(List.<FlowerSummaryResponse>of(), 0, 20, 3),
                null
        );

        assertThat(payload).isPresent();
        assertThat(payload.get())
                .containsEntry("keyword", "장미")
                .containsEntry("totalElements", 3L);
    }
}
