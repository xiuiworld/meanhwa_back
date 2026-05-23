package com.example.meanhwa_back.message.ratelimit;



import java.time.Duration;

import java.time.Instant;

import java.util.concurrent.ConcurrentHashMap;



import com.example.meanhwa_back.message.config.MessageGenerationRateLimitProperties;



import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.stereotype.Component;



/**

 * JVM 메모리 기반 메시지 생성 Rate Limiter.

 *

 * <p>{@code app.message.rate-limit.store=memory}이거나 속성이 없을 때 등록된다 (로컬·테스트 기본).

 * 서버를 재시작하면 카운터가 초기화되며, 스케일 아웃 시 인스턴스마다 quota가 따로 잡힌다.

 *

 * <h2>고정 윈도우 동작</h2>

 * <ol>

 *   <li>userId별로 (count, expiresAt)를 보관한다.</li>

 *   <li>윈도우가 없거나 만료됐으면 count=1, expiresAt=now+window으로 새 윈도우를 연다.</li>

 *   <li>윈도우가 유효하면 count를 1 증가시킨다.</li>

 *   <li>{@code count <= maxRequests}이면 허용, 아니면 거부한다.</li>

 * </ol>

 */

@Component

@ConditionalOnProperty(

        prefix = "app.message.rate-limit",

        name = "store",

        havingValue = "memory",

        matchIfMissing = true

)
/**
 * 단일 JVM에서 동작하는 메시지 생성 고정 윈도우 Rate Limiter.
 * 로컬과 테스트 환경에 적합하며 운영 다중 인스턴스에서는 Redis 구현을 사용한다.
 */

public class InMemoryMessageGenerationRateLimiter implements MessageGenerationRateLimiter {

    private final MessageGenerationRateLimitProperties properties;



    /** userId → 현재 윈도우 상태. */

    private final ConcurrentHashMap<Long, Window> windows = new ConcurrentHashMap<>();



    public InMemoryMessageGenerationRateLimiter(MessageGenerationRateLimitProperties properties) {

        this.properties = properties;

    }
/**
 * 사용자별 메시지 생성 quota를 확인하고 허용되면 현재 요청을 차감한다.
 */



    @Override

    public RateLimitDecision consume(Long userId) {

        Instant now = Instant.now();

        Duration windowDuration = properties.getWindow();

        int limit = properties.getMaxRequests();



        // 동일 userId에 대한 갱신은 compute로 원자적으로 처리

        Window window = windows.compute(userId, (id, current) -> {

            if (current == null || now.isAfter(current.expiresAt())) {

                // 새 윈도우: 이번 요청이 1회차

                return new Window(1, now.plus(windowDuration));

            }

            // 기존 윈도우: 만료 시각은 첫 요청 기준으로 유지

            return new Window(current.count() + 1, current.expiresAt());

        });



        boolean allowed = window.count() <= limit;

        int remaining = Math.max(0, limit - window.count());

        Duration retryAfter = Duration.between(now, window.expiresAt());

        if (retryAfter.isNegative()) {

            retryAfter = Duration.ZERO;

        }

        return new RateLimitDecision(allowed, limit, remaining, retryAfter);

    }



    /**

     * 한 userId에 대한 현재 고정 윈도우 스냅샷.

     *

     * @param count     이 윈도우에서 지금까지 소비한 횟수 (이번 요청 포함)

     * @param expiresAt 이 윈도우가 끝나는 시각 (첫 요청 + window)

     */

    private record Window(int count, Instant expiresAt) {

    }

}

