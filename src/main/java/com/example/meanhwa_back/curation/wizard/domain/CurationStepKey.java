package com.example.meanhwa_back.curation.wizard.domain;

/**
 * 분기형 큐레이션 위저드 6단계 식별자.
 * <p>API path·요청 JSON·플로우 YAML에서 동일한 문자열을 사용한다.
 */
public enum CurationStepKey {
    OCCASION,
    RECIPIENT,
    EMOTION,
    FLOWER_MEANING,
    SPACE,
    BUDGET;

    public static CurationStepKey from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("step key가 비어 있습니다.");
        }
        try {
            return CurationStepKey.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("지원하지 않는 step key: " + value);
        }
    }
}
