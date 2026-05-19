package com.example.meanhwa_back.message.ratelimit;



/**
 * 사용자별 메시지 생성 quota 소비·판정 전략.
 * <p>구현체는 고정 윈도우(fixed window) 방식을 사용한다.
 * 윈도우 내 첫 요청 시 카운터가 시작되고, 만료 후 다시 {@code maxRequests}만큼 허용된다.

 * @see InMemoryMessageGenerationRateLimiter

 * @see RedisMessageGenerationRateLimiter

 */

public interface MessageGenerationRateLimiter {

    /**

     * 해당 사용자의 이번 요청을 quota에 반영하고 허용 여부를 반환한다.
     * <p>호출할 때마다 카운터가 1 증가한다. 한도를 이미 초과한 상태에서 추가 호출해도
     * 카운터는 계속 올라가지만 {@link RateLimitDecision#allowed()}는 false로 유지된다.

     * @param userId 인증된 회원 ID
     */
    
    RateLimitDecision consume(Long userId);

}

