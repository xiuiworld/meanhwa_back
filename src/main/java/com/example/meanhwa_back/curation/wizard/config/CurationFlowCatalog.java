package com.example.meanhwa_back.curation.wizard.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.curation.wizard.domain.CurationStepKey;
import com.example.meanhwa_back.curation.wizard.dto.CurationSelectionDto;
import com.example.meanhwa_back.curation.wizard.dto.CurationWizardOptionDto;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

/**
 * 분기형 큐레이션 플로우 YAML 카탈로그.
 * <p>P1 단계에서는 DB {@code curation_option_rules} 없이 classpath YAML만으로
 * 단계 메타·분기별 선택지·질문 문구를 제공한다.
 */
@Component
public class CurationFlowCatalog {
    private static final String DEFAULT_FLOW_RESOURCE = "curation/flow-2026-05-v1.yml";

    private final CurationFlowDocument document;
    private final Map<CurationStepKey, CurationFlowDocument.StepDocument> stepByKey;

    public CurationFlowCatalog() {
        this.document = loadDocument(DEFAULT_FLOW_RESOURCE);
        this.stepByKey = indexSteps(document);
    }

    public String getDefaultFlowVersion() {
        return document.getFlowVersion();
    }

    public CurationFlowDocument getDocument() {
        return document;
    }

    public CurationFlowDocument.StepDocument requireStep(CurationStepKey stepKey) {
        CurationFlowDocument.StepDocument step = stepByKey.get(stepKey);
        if (step == null) {
            throw new BusinessException(ErrorCode.INVALID_CURATION_STEP);
        }
        return step;
    }

    /**
     * 요청 flowVersion이 비어 있으면 최신(기본) 버전을 사용하고, 알 수 없는 버전이면 404.
     */
    public String resolveFlowVersion(String requestedVersion) {
        if (requestedVersion == null || requestedVersion.isBlank()) {
            return document.getFlowVersion();
        }
        if (!document.getFlowVersion().equals(requestedVersion.trim())) {
            throw new BusinessException(ErrorCode.CURATION_FLOW_NOT_FOUND);
        }
        return document.getFlowVersion();
    }

    /**
     * 해당 단계에서 고를 수 있는 옵션 목록(분기 반영).
     */
    public List<CurationWizardOptionDto> resolveOptions(
            CurationStepKey stepKey,
            Map<CurationStepKey, String> selectionByStep
    ) {
        return switch (stepKey) {
            case OCCASION -> toOptions(document.getOccasionOptions());
            case RECIPIENT -> {
                String occasion = requirePriorCode(selectionByStep, CurationStepKey.OCCASION);
                yield toOptions(requireBranch(document.getRecipientBranches(), occasion, "RECIPIENT"));
            }
            case EMOTION -> {
                String occasion = requirePriorCode(selectionByStep, CurationStepKey.OCCASION);
                yield toOptions(requireBranch(document.getEmotionBranches(), occasion, "EMOTION"));
            }
            case FLOWER_MEANING -> {
                String emotion = requirePriorCode(selectionByStep, CurationStepKey.EMOTION);
                yield buildFlowerMeaningOptions(emotion);
            }
            case SPACE -> toOptions(document.getSpaceOptions());
            case BUDGET -> toOptions(document.getBudgetOptions());
        };
    }

    /**
     * 단계별 동적 질문 제목. FLOWER_MEANING은 RECIPIENT 코드에 따라 문구 분기.
     */
    public String resolveQuestionTitle(CurationStepKey stepKey, Map<CurationStepKey, String> selectionByStep) {
        if (stepKey == CurationStepKey.FLOWER_MEANING) {
            String recipient = selectionByStep.get(CurationStepKey.RECIPIENT);
            if (recipient != null) {
                String override = document.getFlowerMeaningQuestionByRecipient().get(recipient);
                if (override != null) {
                    return override;
                }
            }
            return document.getFlowerMeaningQuestionByRecipient()
                    .getOrDefault("default", requireStep(stepKey).getDefaultQuestionTitle());
        }
        return requireStep(stepKey).getDefaultQuestionTitle();
    }

    public String resolveQuestionSubtitle(CurationStepKey stepKey) {
        return requireStep(stepKey).getDefaultQuestionSubtitle();
    }

    /**
     * 결과 API용: 6단계가 모두 채워졌는지, code가 분기표에 존재하는지 검증한다.
     */
    public Map<CurationStepKey, String> validateCompleteSelections(List<CurationSelectionDto> selections) {
        if (selections == null || selections.size() != document.getTotalSteps()) {
            throw new BusinessException(ErrorCode.INCOMPLETE_CURATION_SELECTION);
        }

        Map<CurationStepKey, String> byStep = new EnumMap<>(CurationStepKey.class);
        for (CurationSelectionDto selection : selections) {
            CurationStepKey stepKey = parseStepKey(selection.step());
            if (byStep.containsKey(stepKey)) {
                throw new BusinessException(ErrorCode.INVALID_CURATION_SELECTION);
            }
            String code = normalizeCode(selection.code());
            assertOptionAllowed(stepKey, byStep, code);
            byStep.put(stepKey, code);
        }

        for (CurationStepKey required : CurationStepKey.values()) {
            if (!byStep.containsKey(required)) {
                throw new BusinessException(ErrorCode.INCOMPLETE_CURATION_SELECTION);
            }
        }
        return byStep;
    }

    /**
     * options API용: 이전 단계 선택이 요구 조건을 만족하는지 검사한다.
     */
    public void validatePriorSelectionsForOptions(CurationStepKey stepKey, Map<CurationStepKey, String> prior) {
        switch (stepKey) {
            case RECIPIENT, EMOTION -> requirePriorCode(prior, CurationStepKey.OCCASION);
            case FLOWER_MEANING -> {
                requirePriorCode(prior, CurationStepKey.OCCASION);
                requirePriorCode(prior, CurationStepKey.RECIPIENT);
                requirePriorCode(prior, CurationStepKey.EMOTION);
            }
            default -> {
                // OCCASION, SPACE, BUDGET — 필수 prior 없음
            }
        }
    }

    public Set<String> allScoringCodes(Map<CurationStepKey, String> completeSelections) {
        return completeSelections.entrySet().stream()
                .filter(entry -> entry.getKey() != CurationStepKey.BUDGET)
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    private void assertOptionAllowed(
            CurationStepKey stepKey,
            Map<CurationStepKey, String> prior,
            String code
    ) {
        List<CurationWizardOptionDto> allowed = resolveOptions(stepKey, prior);
        boolean found = allowed.stream().anyMatch(option -> option.code().equals(code));
        if (!found) {
            throw new BusinessException(ErrorCode.INVALID_CURATION_SELECTION);
        }
    }

    private List<CurationWizardOptionDto> buildFlowerMeaningOptions(String emotionCode) {
        List<String> labels = document.getFlowerMeaningLabels().get(emotionCode);
        if (labels == null || labels.size() != 4) {
            throw new BusinessException(ErrorCode.INVALID_CURATION_SELECTION);
        }
        List<CurationWizardOptionDto> options = new ArrayList<>(4);
        for (int index = 0; index < labels.size(); index++) {
            String code = emotionCode + "_" + (index + 1);
            options.add(new CurationWizardOptionDto(code, labels.get(index), null, null));
        }
        return options;
    }

    private List<CurationFlowDocument.OptionDocument> requireBranch(
            Map<String, List<CurationFlowDocument.OptionDocument>> branches,
            String parentCode,
            String stepLabel
    ) {
        List<CurationFlowDocument.OptionDocument> options = branches.get(parentCode);
        if (options == null || options.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_CURATION_SELECTION,
                    "허용되지 않는 " + stepLabel + " 분기: " + parentCode
            );
        }
        return options;
    }

    private String requirePriorCode(Map<CurationStepKey, String> prior, CurationStepKey requiredStep) {
        String code = prior.get(requiredStep);
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.INCOMPLETE_CURATION_SELECTION);
        }
        return code;
    }

    private List<CurationWizardOptionDto> toOptions(List<CurationFlowDocument.OptionDocument> source) {
        return source.stream()
                .map(option -> new CurationWizardOptionDto(option.getCode(), option.getLabel(), null, null))
                .toList();
    }

    private CurationStepKey parseStepKey(String step) {
        try {
            return CurationStepKey.from(step);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_CURATION_STEP);
        }
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CURATION_SELECTION);
        }
        return code.trim().toUpperCase();
    }

    private static Map<CurationStepKey, CurationFlowDocument.StepDocument> indexSteps(CurationFlowDocument document) {
        Map<CurationStepKey, CurationFlowDocument.StepDocument> map = new EnumMap<>(CurationStepKey.class);
        for (CurationFlowDocument.StepDocument step : document.getSteps()) {
            map.put(CurationStepKey.from(step.getKey()), step);
        }
        return map;
    }

    private static CurationFlowDocument loadDocument(String classpathLocation) {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        Yaml yaml = new Yaml();
        try (InputStream inputStream = resource.getInputStream()) {
            CurationFlowDocument loaded = yaml.loadAs(inputStream, CurationFlowDocument.class);
            if (loaded == null) {
                throw new IllegalStateException("YAML이 비어 있습니다: " + classpathLocation);
            }
            if (loaded.getFlowVersion() == null || loaded.getFlowVersion().isBlank()) {
                throw new IllegalStateException("flowVersion이 비어 있습니다: " + classpathLocation);
            }
            return loaded;
        } catch (IOException exception) {
            throw new IllegalStateException("큐레이션 플로우 YAML 로드 실패: " + classpathLocation, exception);
        }
    }
}
