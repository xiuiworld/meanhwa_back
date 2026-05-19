package com.example.meanhwa_back.curation.wizard.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code classpath:curation/flow-2026-05-v1.yml} 역직렬화 모델.
 * <p>런타임 분기 검증·옵션 목록의 단일 출처(single source of truth)이다.
 */
public class CurationFlowDocument {
    private String flowVersion;
    private int totalSteps;
    private List<StepDocument> steps = List.of();
    private List<OptionDocument> occasionOptions = List.of();
    private Map<String, List<OptionDocument>> recipientBranches = Map.of();
    private Map<String, List<OptionDocument>> emotionBranches = Map.of();
    private Map<String, List<String>> flowerMeaningLabels = Map.of();
    private List<OptionDocument> spaceOptions = List.of();
    private List<OptionDocument> budgetOptions = List.of();
    private Map<String, String> flowerMeaningQuestionByRecipient = Map.of();

    public String getFlowVersion() {
        return flowVersion;
    }

    public void setFlowVersion(String flowVersion) {
        this.flowVersion = flowVersion;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public List<StepDocument> getSteps() {
        return steps;
    }

    public void setSteps(List<StepDocument> steps) {
        this.steps = steps == null ? List.of() : List.copyOf(steps);
    }

    public List<OptionDocument> getOccasionOptions() {
        return occasionOptions;
    }

    public void setOccasionOptions(List<OptionDocument> occasionOptions) {
        this.occasionOptions = occasionOptions == null ? List.of() : List.copyOf(occasionOptions);
    }

    public Map<String, List<OptionDocument>> getRecipientBranches() {
        return recipientBranches;
    }

    public void setRecipientBranches(Map<String, List<OptionDocument>> recipientBranches) {
        this.recipientBranches = recipientBranches == null ? Map.of() : Map.copyOf(recipientBranches);
    }

    public Map<String, List<OptionDocument>> getEmotionBranches() {
        return emotionBranches;
    }

    public void setEmotionBranches(Map<String, List<OptionDocument>> emotionBranches) {
        this.emotionBranches = emotionBranches == null ? Map.of() : Map.copyOf(emotionBranches);
    }

    public Map<String, List<String>> getFlowerMeaningLabels() {
        return flowerMeaningLabels;
    }

    public void setFlowerMeaningLabels(Map<String, List<String>> flowerMeaningLabels) {
        this.flowerMeaningLabels = flowerMeaningLabels == null ? Map.of() : new LinkedHashMap<>(flowerMeaningLabels);
    }

    public List<OptionDocument> getSpaceOptions() {
        return spaceOptions;
    }

    public void setSpaceOptions(List<OptionDocument> spaceOptions) {
        this.spaceOptions = spaceOptions == null ? List.of() : List.copyOf(spaceOptions);
    }

    public List<OptionDocument> getBudgetOptions() {
        return budgetOptions;
    }

    public void setBudgetOptions(List<OptionDocument> budgetOptions) {
        this.budgetOptions = budgetOptions == null ? List.of() : List.copyOf(budgetOptions);
    }

    public Map<String, String> getFlowerMeaningQuestionByRecipient() {
        return flowerMeaningQuestionByRecipient;
    }

    public void setFlowerMeaningQuestionByRecipient(Map<String, String> flowerMeaningQuestionByRecipient) {
        this.flowerMeaningQuestionByRecipient = flowerMeaningQuestionByRecipient == null
                ? Map.of()
                : Map.copyOf(flowerMeaningQuestionByRecipient);
    }

    public static class StepDocument {
        private String key;
        private int order;
        private String defaultQuestionTitle;
        private String defaultQuestionSubtitle;
        private String selectionMode;
        private List<String> dependsOn = List.of();

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public String getDefaultQuestionTitle() {
            return defaultQuestionTitle;
        }

        public void setDefaultQuestionTitle(String defaultQuestionTitle) {
            this.defaultQuestionTitle = defaultQuestionTitle;
        }

        public String getDefaultQuestionSubtitle() {
            return defaultQuestionSubtitle;
        }

        public void setDefaultQuestionSubtitle(String defaultQuestionSubtitle) {
            this.defaultQuestionSubtitle = defaultQuestionSubtitle;
        }

        public String getSelectionMode() {
            return selectionMode;
        }

        public void setSelectionMode(String selectionMode) {
            this.selectionMode = selectionMode;
        }

        public List<String> getDependsOn() {
            return dependsOn;
        }

        public void setDependsOn(List<String> dependsOn) {
            this.dependsOn = dependsOn == null ? List.of() : List.copyOf(dependsOn);
        }
    }

    public static class OptionDocument {
        private String code;
        private String label;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
