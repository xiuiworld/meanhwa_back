package com.example.meanhwa_back.curation.wizard.domain;

import java.util.Map;
import java.util.Optional;

import com.example.meanhwa_back.flower.domain.PriceRange;

/**
 * Step6 예산 선택 코드 → {@link PriceRange} 필터 매핑.
 * <p>예산 단계는 태그 점수에 포함하지 않고 {@code flowers.price_range} 필터로만 사용한다.
 */
public enum CurationBudgetCode {
    BUDGET_LOW(PriceRange.LOW),
    BUDGET_MEDIUM(PriceRange.MEDIUM),
    BUDGET_HIGH(PriceRange.HIGH),
    BUDGET_PREMIUM(PriceRange.PREMIUM);

    private static final Map<String, CurationBudgetCode> BY_CODE = Map.of(
            "BUDGET_LOW", BUDGET_LOW,
            "BUDGET_MEDIUM", BUDGET_MEDIUM,
            "BUDGET_HIGH", BUDGET_HIGH,
            "BUDGET_PREMIUM", BUDGET_PREMIUM
    );

    private final PriceRange priceRange;

    CurationBudgetCode(PriceRange priceRange) {
        this.priceRange = priceRange;
    }

    public PriceRange getPriceRange() {
        return priceRange;
    }

    /**
     * 클라이언트가 보낸 예산 option code를 내부 예산 enum으로 변환한다.
     */
    public static Optional<CurationBudgetCode> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_CODE.get(code.trim().toUpperCase()));
    }
}
