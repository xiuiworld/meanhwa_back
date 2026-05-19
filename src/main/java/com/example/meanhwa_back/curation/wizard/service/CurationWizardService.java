package com.example.meanhwa_back.curation.wizard.service;



import java.util.List;

import java.util.Map;



import com.example.meanhwa_back.common.error.BusinessException;

import com.example.meanhwa_back.common.error.ErrorCode;

import com.example.meanhwa_back.common.response.PageResponse;

import com.example.meanhwa_back.curation.dto.CurationFlowerResponse;

import com.example.meanhwa_back.curation.service.CurationService;

import com.example.meanhwa_back.curation.wizard.config.CurationFlowCatalog;

import com.example.meanhwa_back.curation.wizard.config.CurationFlowDocument;

import com.example.meanhwa_back.curation.wizard.domain.CurationStepKey;

import com.example.meanhwa_back.curation.wizard.dto.CurationFlowResponse;

import com.example.meanhwa_back.curation.wizard.dto.CurationFlowStepResponse;

import com.example.meanhwa_back.curation.wizard.dto.CurationSelectionDto;

import com.example.meanhwa_back.curation.wizard.dto.CurationStepOptionsResponse;

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



    public CurationWizardService(

            CurationFlowCatalog flowCatalog,

            CurationCodeResolver codeResolver,

            CurationSelectionParser selectionParser,

            CurationService curationService

    ) {

        this.flowCatalog = flowCatalog;

        this.codeResolver = codeResolver;

        this.selectionParser = selectionParser;

        this.curationService = curationService;

    }



    /**

     * 플로우 메타 전체 (오프라인 캐시·스토리북용).

     */

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



    /**

     * 특정 단계의 선택지 + 동적 질문 문구.

     */

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

     * <p>성공 시 {@link LogAction} AOP가 {@link ActionType#CURATION_START} (source=curation-v2) 를 남긴다.

     */

    @LogAction(value = ActionType.CURATION_START, extractor = WizardCurationStartPayloadExtractor.class)

    public PageResponse<CurationFlowerResponse> getResults(CurationWizardResultsRequest request) {

        flowCatalog.resolveFlowVersion(request.flowVersion());

        Map<CurationStepKey, String> selections = flowCatalog.validateCompleteSelections(request.selections());



        String budgetCode = selections.get(CurationStepKey.BUDGET);

        PriceRange priceRange = codeResolver.resolveBudgetPriceRange(budgetCode);



        List<Long> tagIds = codeResolver.resolveScoringTagIds(flowCatalog.allScoringCodes(selections));

        return curationService.executeCurate(

                tagIds,

                null,

                priceRange.name(),

                request.resolvedPage(),

                request.resolvedSize()

        );

    }



    private CurationStepKey parseStepKey(String stepKeyValue) {

        try {

            return CurationStepKey.from(stepKeyValue);

        } catch (IllegalArgumentException exception) {

            throw new BusinessException(ErrorCode.INVALID_CURATION_STEP);

        }

    }

}

