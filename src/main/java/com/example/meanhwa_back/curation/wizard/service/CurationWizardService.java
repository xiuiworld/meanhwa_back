package com.example.meanhwa_back.curation.wizard.service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.curation.dto.CurationFlowerResponse;
import com.example.meanhwa_back.curation.history.domain.UserCurationResult;
import com.example.meanhwa_back.curation.history.dto.CurationSelectionSnapshot;
import com.example.meanhwa_back.curation.history.service.CurationResultHistoryService;
import com.example.meanhwa_back.curation.service.CurationService;
import com.example.meanhwa_back.curation.wizard.config.CurationFlowCatalog;
import com.example.meanhwa_back.curation.wizard.domain.CurationStepKey;
import com.example.meanhwa_back.curation.wizard.dto.CurationResultsResponse;
import com.example.meanhwa_back.curation.wizard.dto.CurationWizardOptionDto;
import com.example.meanhwa_back.curation.wizard.dto.CurationWizardResultsRequest;
import com.example.meanhwa_back.flower.domain.PriceRange;
import com.example.meanhwa_back.log.aop.LogAction;
import com.example.meanhwa_back.log.aop.extractor.WizardCurationStartPayloadExtractor;
import com.example.meanhwa_back.log.domain.ActionType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 분기형 큐레이션 위저드 API 오케스트레이션.
 *
 * <ul>
 *   <li>{@link CurationFlowCatalog} YAML 기반 selection 검증</li>
 *   <li>{@link CurationCodeResolver}로 code→tagId 후 {@link CurationService} 점수 합산 재사용</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class CurationWizardService {
    private final CurationFlowCatalog flowCatalog;
    private final CurationCodeResolver codeResolver;
    private final CurationService curationService;
    private final CurationResultHistoryService curationResultHistoryService;

    public CurationWizardService(
            CurationFlowCatalog flowCatalog,
            CurationCodeResolver codeResolver,
            CurationService curationService,
            CurationResultHistoryService curationResultHistoryService
    ) {
        this.flowCatalog = flowCatalog;
        this.codeResolver = codeResolver;
        this.curationService = curationService;
        this.curationResultHistoryService = curationResultHistoryService;
    }

    /**
     * 6단계 완료 선택 → 기존 큐레이션 점수 엔진으로 꽃 목록 반환.
     * <p>성공 시 {@link LogAction} AOP가 {@link ActionType#CURATION_START} (source=curation-v2) 를 남긴다.
     */
    @LogAction(value = ActionType.CURATION_START, extractor = WizardCurationStartPayloadExtractor.class)
    @Transactional
    public CurationResultsResponse getResults(CurationWizardResultsRequest request) {
        String flowVersion = flowCatalog.resolveFlowVersion(request.flowVersion());
        Map<CurationStepKey, String> selections = flowCatalog.validateCompleteSelections(request.selections());

        String budgetCode = selections.get(CurationStepKey.BUDGET);
        PriceRange priceRange = codeResolver.resolveBudgetPriceRange(budgetCode);

        List<Long> tagIds = codeResolver.resolveScoringTagIds(flowCatalog.allScoringCodes(selections));
        PageResponse<CurationFlowerResponse> response = curationService.executeCurate(
                tagIds,
                null,
                priceRange.name(),
                request.resolvedPage(),
                request.resolvedSize()
        );
        UserCurationResult savedResult =
                curationResultHistoryService.saveIfAuthenticated(flowVersion, toSelectionSnapshots(selections), response);
        return CurationResultsResponse.from(response, savedResult == null ? null : savedResult.getId());
    }

    private List<CurationSelectionSnapshot> toSelectionSnapshots(Map<CurationStepKey, String> selections) {
        Map<CurationStepKey, String> prior = new EnumMap<>(CurationStepKey.class);
        return Arrays.stream(CurationStepKey.values())
                .map(stepKey -> {
                    String code = selections.get(stepKey);
                    String label = flowCatalog.resolveOptions(stepKey, prior)
                            .stream()
                            .filter(option -> option.code().equals(code))
                            .findFirst()
                            .map(CurationWizardOptionDto::label)
                            .orElse(code);
                    prior.put(stepKey, code);
                    return new CurationSelectionSnapshot(stepKey.name(), code, label);
                })
                .toList();
    }

}
