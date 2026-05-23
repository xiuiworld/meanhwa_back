package com.example.meanhwa_back.message.ratelimit;

import java.time.Duration;

/**
 * Rate Limit 1회 검사 결과.
 *
 * @param allowed     이번 요청을 허용할지 여부 ({@code count <= limit})
 * @param limit       설정된 최대 허용 횟수
 * @param remaining   현재 윈도우에서 남은 허용 횟수 (초과 시 0)
 * @param retryAfter  현재 윈도우가 끝나기까지 남은 시간 (클라이언트 재시도 안내용)
 */
public record RateLimitDecision(
        boolean allowed,
        int limit,
        int remaining,
        Duration retryAfter
) {
}
