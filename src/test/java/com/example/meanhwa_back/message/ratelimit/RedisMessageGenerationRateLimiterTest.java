package com.example.meanhwa_back.message.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.meanhwa_back.message.config.MessageGenerationRateLimitProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

/** Redis 기반 메시지 생성 Rate Limiter의 quota·TTL 계산 단위 테스트. */
@ExtendWith(MockitoExtension.class)
class RedisMessageGenerationRateLimiterTest {
    @Mock
    private StringRedisTemplate redisTemplate;

    private MessageGenerationRateLimitProperties properties;
    private RedisMessageGenerationRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        properties = new MessageGenerationRateLimitProperties();
        properties.setMaxRequests(2);
        properties.setWindow(Duration.ofMinutes(5));
        rateLimiter = new RedisMessageGenerationRateLimiter(redisTemplate, properties);
    }

    @Test
    void allowsRequestsWithinLimitAndBlocksOverLimit() {
        stubConsume("message-gen:1", 1L, 2L, 3L);
        when(redisTemplate.getExpire("message-gen:1", TimeUnit.SECONDS)).thenReturn(300L);

        assertThat(rateLimiter.consume(1L).allowed()).isTrue();
        assertThat(rateLimiter.consume(1L).allowed()).isTrue();

        RateLimitDecision blocked = rateLimiter.consume(1L);
        assertThat(blocked.allowed()).isFalse();
        assertThat(blocked.remaining()).isZero();
        assertThat(blocked.retryAfter()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void isolatesLimitsPerUserKey() {
        stubConsume("message-gen:1", 3L);
        stubConsume("message-gen:2", 1L);
        when(redisTemplate.getExpire("message-gen:1", TimeUnit.SECONDS)).thenReturn(120L);
        when(redisTemplate.getExpire("message-gen:2", TimeUnit.SECONDS)).thenReturn(300L);

        assertThat(rateLimiter.consume(1L).allowed()).isFalse();
        assertThat(rateLimiter.consume(2L).allowed()).isTrue();

        verify(redisTemplate).execute(
                anyRedisScript(),
                eq(List.of("message-gen:1")),
                eq(String.valueOf(properties.getWindow().toMillis()))
        );
        verify(redisTemplate).execute(
                anyRedisScript(),
                eq(List.of("message-gen:2")),
                eq(String.valueOf(properties.getWindow().toMillis()))
        );
    }

    private void stubConsume(String key, Long... counts) {
        Long first = counts[0];
        Long[] rest = Arrays.copyOfRange(counts, 1, counts.length);
        when(redisTemplate.execute(
                anyRedisScript(),
                eq(List.of(key)),
                eq(String.valueOf(properties.getWindow().toMillis()))
        )).thenReturn(first, rest);
    }

    @SuppressWarnings("unchecked")
    private RedisScript<Long> anyRedisScript() {
        return any(RedisScript.class);
    }
}
