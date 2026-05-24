package com.example.meanhwa_back.curation.wizard.service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.curation.dto.CurationFlowerResponse;
import com.example.meanhwa_back.curation.history.domain.UserCurationResult;
import com.example.meanhwa_back.curation.history.dto.CurationSelectionSnapshot;
import com.example.meanhwa_back.curation.history.service.CurationResultHistoryService;
import com.example.meanhwa_back.curation.service.CurationService;
import com.example.meanhwa_back.curation.wizard.config.CurationFlowCatalog;
import com.example.meanhwa_back.curation.wizard.config.CurationFlowDocument;
import com.example.meanhwa_back.curation.wizard.domain.CurationStepKey;
import com.example.meanhwa_back.curation.wizard.dto.CurationFlowResponse;
import com.example.meanhwa_back.curation.wizard.dto.CurationFlowStepResponse;
import com.example.meanhwa_back.curation.wizard.dto.CurationStepOptionsResponse;
import com.example.meanhwa_back.curation.wizard.dto.CurationWizardOptionDto;
import com.example.meanhwa_back.curation.wizard.dto.CurationWizardResultsRequest;
import com.example.meanhwa_back.curation.wizard.dto.CurationWizardResultsResponse;
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
 *   <li>P1: {@link CurationFlowCatalog} YAML 기반 flow·options</li>
 *   <li>P2: {@link CurationCodeResolver}로 code→tagId 후 {@link CurationService} 점수 합산 재사용</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class CurationWizardService {
    private final CurationFlowCatalog flowCatalog;
    private final CurationCodeResolver codeResolver;
    private final CurationSelectionParser selectionParser;
    private final CurationService curationService;
    private final CurationResultHistoryService curationResultHistoryService;

    public CurationWizardService(
            CurationFlowCatalog flowCatalog,
            CurationCodeResolver codeResolver,
            CurationSelectionParser selectionParser,
            CurationService curationService,
            CurationResultHistoryService curationResultHistoryService
    ) {
        this.flowCatalog = flowCatalog;
        this.codeResolver = codeResolver;
        this.selectionParser = selectionParser;
        this.curationService = curationService;
        this.curationResultHistoryService = curationResultHistoryService;
    }

    /** 플로우 메타 전체 (오프라인 캐시·스토리북용). */
    public CurationFlowResponse getFlow(String requestedFlowVersion) {
        String flowVersion = flowCatalog.resolveFlowVersion(requestedFlowVersion);
        CurationFlowDocument document = flowCatalog.getDocument();
        List<CurationFlowStepResponse> steps = document.getSteps().stream()
                .map(step -> new CurationFlowStepResponse(
                        step.getKey(),
                        step.getOrder(),
                        step.getDefaultQuestionTitle(),
                        step.getDefaultQuestionSubtitle(),
                        step.getSelectionMode(),
                        step.getDependsOn()
                ))
                .toList();
        return new CurationFlowResponse(flowVersion, document.getTotalSteps(), steps);
    }

    /** 특정 단계의 선택지 + 동적 질문 문구. */
    public CurationStepOptionsResponse getStepOptions(
            String stepKeyValue,
            String requestedFlowVersion,
            String selectionsJson
    ) {
        String flowVersion = flowCatalog.resolveFlowVersion(requestedFlowVersion);
        CurationStepKey stepKey = parseStepKey(stepKeyValue);
        Map<CurationStepKey, String> priorSelections = selectionParser.parseToMap(selectionsJson);
        flowCatalog.validatePriorSelectionsForOptions(stepKey, priorSelections);

        List<CurationWizardOptionDto> options = flowCatalog.resolveOptions(stepKey, priorSelections);
        options = codeResolver.enrichWithTagIds(options);

        CurationFlowDocument.StepDocument stepMeta = flowCatalog.requireStep(stepKey);
        return new CurationStepOptionsResponse(
                flowVersion,
                stepKey.name(),
                stepMeta.getOrder(),
                flowCatalog.resolveQuestionTitle(stepKey, priorSelections),
                flowCatalog.resolveQuestionSubtitle(stepKey),
                options
        );
    }

    /**
     * 6단계 완료 선택 → 기존 큐레이션 점수 엔진으로 꽃 목록 반환.
     *
     * <p>인증은 optional이다. Bearer가 있으면 {@link CurationResultHistoryService#saveIfAuthenticated}
     * 로 snapshot을 저장하고, 저장된 row id를 {@code curationResultId}로 응답에 실어 준다.
     * 저장 실패해도 추천 목록(HTTP 200)은 그대로 반환한다.
     *
     * <p>성공 시 {@link LogAction} AOP가 {@link ActionType#CURATION_START} (source=curation-v2) 를 남긴다.
     */
    @LogAction(value = ActionType.CURATION_START, extractor = WizardCurationStartPayloadExtractor.class)
    @Transactional
    public CurationWizardResultsResponse getResults(CurationWizardResultsRequest request) {
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
        UserCurationResult saved = curationResultHistoryService.saveIfAuthenticated(
                flowVersion,
                toSelectionSnapshots(selections),
                response
        );
        // 비로그인(saved == null)이면 curationResultId 생략 → 기존 page-only 응답과 동일하게 보임
        Long curationResultId = saved != null ? saved.getId() : null;
        return CurationWizardResultsResponse.from(response, curationResultId);
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

    private CurationStepKey parseStepKey(String stepKeyValue) {
        try {
            return CurationStepKey.from(stepKeyValue);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_CURATION_STEP);
        }
    }
}
