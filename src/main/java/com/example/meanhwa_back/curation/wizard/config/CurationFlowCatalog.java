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
/**
 * 현재 선택 경로에 맞는 위저드 질문 보조 문구를 계산한다.
 */

    public String resolveQuestionSubtitle(CurationStepKey stepKey) {
        return requireStep(stepKey).getDefaultQuestionSubtitle();
    }

    /**
     * 결과 API용: 6단계가 모두 채워졌는지, code가 분기표에 존재하는지 검증한다.
     * <p>{@code selections} 배열 순서와 무관하게, 전체 선택을 Map에 모은 뒤 검증한다.
     */
    public Map<CurationStepKey, String> validateCompleteSelections(List<CurationSelectionDto> selections) {
        if (selections == null) {
            throw new BusinessException(ErrorCode.INCOMPLETE_CURATION_SELECTION);
        }

        Map<CurationStepKey, String> byStep = parseSelectionListToMap(selections);
        for (CurationStepKey required : CurationStepKey.values()) {
            if (!byStep.containsKey(required)) {
                throw new BusinessException(ErrorCode.INCOMPLETE_CURATION_SELECTION);
            }
        }
        validateSelectionPath(byStep);
        return byStep;
    }

    /**
     * options API용: 이전 단계 선택이 요구 조건을 만족하는지 검사한다.
     * <p>Step4({@link CurationStepKey#FLOWER_MEANING})는 꽃말 목록을 {@code EMOTION} 코드만으로 만들기 때문에,
     * 키 존재 여부뿐 아니라 {@code OCCASION} 분기표와 맞는 {@code RECIPIENT}/{@code EMOTION} 인지도 검증한다.
     */
    public void validatePriorSelectionsForOptions(CurationStepKey stepKey, Map<CurationStepKey, String> prior) {
        rejectCurrentOrFutureSelections(stepKey, prior);
        validateSelectionPath(prior);

        switch (stepKey) {
            case RECIPIENT, EMOTION -> requirePriorCode(prior, CurationStepKey.OCCASION);
            case FLOWER_MEANING -> assertFlowerMeaningPriorSelectionsConsistent(prior);
            default -> {
                // OCCASION, SPACE, BUDGET — 필수 prior 없음
            }
        }
    }

    /**
     * Step4 options 요청 전: 1~3단계 선택이 YAML 분기표와 일치하는지 확인한다.
     * <p>예) {@code OCCASION=PROMOTION} 인데 {@code EMOTION=LOVE} 는 승진 분기에 없으므로 거부.
     */
    private void assertFlowerMeaningPriorSelectionsConsistent(Map<CurationStepKey, String> prior) {
        requirePriorCode(prior, CurationStepKey.OCCASION);
        String recipientCode = requirePriorCode(prior, CurationStepKey.RECIPIENT);
        String emotionCode = requirePriorCode(prior, CurationStepKey.EMOTION);

        // RECIPIENT·EMOTION 각각 해당 OCCASION 분기의 허용 code 목록에 있는지 (Step2/3 options 와 동일 규칙)
        assertOptionAllowed(CurationStepKey.RECIPIENT, prior, recipientCode);
        assertOptionAllowed(CurationStepKey.EMOTION, prior, emotionCode);
    }
/**
 * 큐레이션 점수 계산에 참여하는 모든 option code를 flow 문서에서 추출한다.
 */

    public Set<String> allScoringCodes(Map<CurationStepKey, String> completeSelections) {
        return completeSelections.entrySet().stream()
                .filter(entry -> entry.getKey() != CurationStepKey.BUDGET)
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    /**
     * {@code code}가 {@code stepKey}의 분기 옵션 목록(이전 단계 {@code prior} 반영)에 포함되는지 검사한다.
     */
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

    private Map<CurationStepKey, String> parseSelectionListToMap(List<CurationSelectionDto> selections) {
        Map<CurationStepKey, String> byStep = new EnumMap<>(CurationStepKey.class);
        for (CurationSelectionDto selection : selections) {
            if (selection == null) {
                throw new BusinessException(ErrorCode.INVALID_CURATION_SELECTION);
            }
            CurationStepKey stepKey = parseStepKey(selection.step());
            if (byStep.containsKey(stepKey)) {
                throw new BusinessException(ErrorCode.INVALID_CURATION_SELECTION);
            }
            byStep.put(stepKey, normalizeCode(selection.code()));
        }
        return byStep;
    }

    private void validateSelectionPath(Map<CurationStepKey, String> selections) {
        Map<CurationStepKey, String> validatedPrior = new EnumMap<>(CurationStepKey.class);
        for (CurationStepKey stepKey : CurationStepKey.values()) {
            String code = selections.get(stepKey);
            if (code == null) {
                continue;
            }
            assertOptionAllowed(stepKey, validatedPrior, code);
            validatedPrior.put(stepKey, code);
        }
    }

    private void rejectCurrentOrFutureSelections(
            CurationStepKey requestedStep,
            Map<CurationStepKey, String> prior
    ) {
        for (CurationStepKey selectedStep : prior.keySet()) {
            if (selectedStep.ordinal() >= requestedStep.ordinal()) {
                throw new BusinessException(ErrorCode.INVALID_CURATION_SELECTION);
            }
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
            options.add(new CurationWizardOptionDto(code, labels.get(index)));
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
                .map(option -> new CurationWizardOptionDto(option.getCode(), option.getLabel()))
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
            normalizeYamlTypes(loaded);
            return loaded;
        } catch (IOException exception) {
            throw new IllegalStateException("큐레이션 플로우 YAML 로드 실패: " + classpathLocation, exception);
        }
    }

    /**
     * SnakeYAML은 중첩 리스트 항목을 {@link CurationFlowDocument.OptionDocument} 대신 Map으로 넣는 경우가 있어
     * 런타임 {@link ClassCastException}을 방지한다.
     */
    private static void normalizeYamlTypes(CurationFlowDocument document) {
        document.setOccasionOptions(coerceOptionList(document.getOccasionOptions()));
        document.setRecipientBranches(coerceBranchMap(document.getRecipientBranches()));
        document.setEmotionBranches(coerceBranchMap(document.getEmotionBranches()));
        document.setSpaceOptions(coerceOptionList(document.getSpaceOptions()));
        document.setBudgetOptions(coerceOptionList(document.getBudgetOptions()));
    }

    private static Map<String, List<CurationFlowDocument.OptionDocument>> coerceBranchMap(
            Map<String, List<CurationFlowDocument.OptionDocument>> branches
    ) {
        if (branches == null || branches.isEmpty()) {
            return Map.of();
        }
        Map<String, List<CurationFlowDocument.OptionDocument>> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, List<CurationFlowDocument.OptionDocument>> entry : branches.entrySet()) {
            normalized.put(entry.getKey(), coerceOptionList(entry.getValue()));
        }
        return Map.copyOf(normalized);
    }

    private static List<CurationFlowDocument.OptionDocument> coerceOptionList(
            List<CurationFlowDocument.OptionDocument> options
    ) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }
        List<CurationFlowDocument.OptionDocument> normalized = new ArrayList<>(options.size());
        for (Object option : options) {
            normalized.add(coerceOption(option));
        }
        return List.copyOf(normalized);
    }

    private static CurationFlowDocument.OptionDocument coerceOption(Object option) {
        if (option instanceof CurationFlowDocument.OptionDocument document) {
            return document;
        }
        if (option instanceof Map<?, ?> map) {
            CurationFlowDocument.OptionDocument document = new CurationFlowDocument.OptionDocument();
            Object code = map.get("code");
            Object label = map.get("label");
            if (code != null) {
                document.setCode(String.valueOf(code));
            }
            if (label != null) {
                document.setLabel(String.valueOf(label));
            }
            return document;
        }
        throw new IllegalStateException("지원하지 않는 옵션 타입: " + option.getClass().getName());
    }
}
