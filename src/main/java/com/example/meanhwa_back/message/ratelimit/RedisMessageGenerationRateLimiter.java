package com.example.meanhwa_back.message.ratelimit;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.meanhwa_back.message.config.MessageGenerationRateLimitProperties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/**
 * Redis 기반 메시지 생성 Rate Limiter.
 *
 * <p>{@code app.message.rate-limit.store=redis}일 때 등록된다. 운영(prod)에서
 * 여러 애플리케이션 인스턴스가 동일한 userId quota를 공유할 때 사용한다.
 *
 * <p>키 형식: {@code message-gen:{userId}}. 값은 정수 카운터이며,
 * 윈도우 첫 INCR 시 {@link MessageGenerationRateLimitProperties#getWindow()}만큼 TTL이 설정된다.
 */
@Component
@ConditionalOnProperty(prefix = "app.message.rate-limit", name = "store", havingValue = "redis")
public class RedisMessageGenerationRateLimiter implements MessageGenerationRateLimiter {
    /**
     * INCR과 최초 TTL 설정을 한 번에 수행해 레이스 컨디션을 줄인다.
     *
     * <ul>
     *   <li>KEYS[1]: 카운터 키</li>
     *   <li>ARGV[1]: 윈도우 길이(밀리초)</li>
     * </ul>
     */
    private static final DefaultRedisScript<Long> CONSUME_SCRIPT = new DefaultRedisScript<>(
            """
                    local count = redis.call('INCR', KEYS[1])
                    if count == 1 then
                      redis.call('PEXPIRE', KEYS[1], ARGV[1])
                    end
                    return count
                    """,
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final MessageGenerationRateLimitProperties properties;

    public RedisMessageGenerationRateLimiter(
            StringRedisTemplate redisTemplate,
            MessageGenerationRateLimitProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }
/**
 * 사용자별 메시지 생성 quota를 확인하고 허용되면 현재 요청을 차감한다.
 */

    @Override
    public RateLimitDecision consume(Long userId) {
        String key = "message-gen:" + userId;
        long windowMillis = properties.getWindow().toMillis();
        int limit = properties.getMaxRequests();

        Long count = redisTemplate.execute(
                CONSUME_SCRIPT,
                List.of(key),
                String.valueOf(windowMillis)
        );
        long currentCount = count == null ? 0L : count;

        // getExpire는 초(Long)를 반환한다. -1(만료 없음), -2(키 없음)이면 설정 window로 fallback
        Duration retryAfter = resolveRetryAfter(key);

        boolean allowed = currentCount <= limit;
        int remaining = (int) Math.max(0, limit - currentCount);
        return new RateLimitDecision(allowed, limit, remaining, retryAfter);
    }

    private Duration resolveRetryAfter(String key) {
        Long expireSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (expireSeconds == null || expireSeconds <= 0) {
            return properties.getWindow();
        }
        return Duration.ofSeconds(expireSeconds);
    }
}
