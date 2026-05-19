package com.example.meanhwa_back.tag.domain;

/** 태그 분류 (이벤트, 관계, 감정 등). */
public enum TagCategory {
    EVENT,
    RELATION,
    EMOTION,
    /** Step4 꽃말 결(세부 메시지) 전용. */
    MEANING,
    STYLE,
    CARE,
    SEASON,
    ENVIRONMENT
}
