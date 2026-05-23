package com.example.meanhwa_back.flower.domain;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;

/** 큐레이션·꽃 데이터의 예산 구간. */
public enum PriceRange {
    LOW,
    MEDIUM,
    HIGH,
    PREMIUM;
/**
 * 외부 입력 문자열을 내부 enum 값으로 안전하게 변환한다.
 */

    public static PriceRange from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return PriceRange.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_PRICE_RANGE);
        }
    }
}
