package com.example.meanhwa_back.auth.domain;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;

/** 소셜 로그인 제공자 (DEV는 비-prod 전용). */
public enum OAuthProvider {
    DEV,
    KAKAO,
    NAVER;

    /**
     * 외부 입력 문자열을 내부 enum 값으로 안전하게 변환한다.
     */
    public static OAuthProvider from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }

        try {
            return OAuthProvider.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }
    }
}
