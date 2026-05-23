package com.example.meanhwa_back.log.aop.extractor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.meanhwa_back.curation.dto.CurationFlowerResponse;
import com.example.meanhwa_back.curation.wizard.config.CurationFlowCatalog;
import com.example.meanhwa_back.curation.wizard.domain.CurationStepKey;
import com.example.meanhwa_back.curation.wizard.dto.CurationResultsResponse;
import com.example.meanhwa_back.curation.wizard.dto.CurationSelectionDto;
import com.example.meanhwa_back.curation.wizard.dto.CurationWizardResultsRequest;
import com.example.meanhwa_back.curation.wizard.service.CurationCodeResolver;
import com.example.meanhwa_back.flower.domain.PriceRange;
import com.example.meanhwa_back.log.aop.LogAction;

import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

/**
 * 위저드 {@code POST /api/v1/curation/results} → {@code CurationWizardService#getResults} 용 payload.
 *
 * <p>{@code source=curation-v2}, 6단계 {@code selections} 스냅샷을 함께 남긴다.
 */
@Component
public class WizardCurationStartPayloadExtractor implements ActionLogPayloadExtractor {

    private final CurationFlowCatalog flowCatalog;
    private final CurationCodeResolver codeResolver;

    public WizardCurationStartPayloadExtractor(
            CurationFlowCatalog flowCatalog,
            CurationCodeResolver codeResolver
    ) {
        this.flowCatalog = flowCatalog;
        this.codeResolver = codeResolver;
    }
/**
 * 컨트롤러 호출 인자를 행동 로그 actionData JSON으로 변환한다.
 */

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> extract(JoinPoint joinPoint, Object returnValue, LogAction logAction) {
        CurationWizardResultsRequest request = (CurationWizardResultsRequest) joinPoint.getArgs()[0];
        CurationResultsResponse response = (CurationResultsResponse) returnValue;

        Map<CurationStepKey, String> selectionsByStep =
                flowCatalog.validateCompleteSelections(request.selections());
        List<Long> tagIds = codeResolver.resolveScoringTagIds(flowCatalog.allScoringCodes(selectionsByStep));
        String budgetCode = selectionsByStep.get(CurationStepKey.BUDGET);
        PriceRange priceRange = codeResolver.resolveBudgetPriceRange(budgetCode);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("source", "curation-v2");
        payload.put("flowVersion", flowCatalog.resolveFlowVersion(request.flowVersion()));
        payload.put("selections", request.selections().stream()
                .map(this::toSelectionMap)
                .toList());
        payload.put("tagIds", tagIds);
        payload.put("priceRange", priceRange.name());
        payload.put("page", request.resolvedPage());
        payload.put("size", request.resolvedSize());
        payload.put("resultCount", response.content().size());
        payload.put("totalElements", response.totalElements());
        payload.put("resultFlowerIds", response.content().stream()
                .map(CurationFlowerResponse::flowerId)
                .toList());
        return Optional.of(payload);
    }

    private Map<String, String> toSelectionMap(CurationSelectionDto selection) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("step", selection.step());
        map.put("code", selection.code());
        return map;
    }
}
