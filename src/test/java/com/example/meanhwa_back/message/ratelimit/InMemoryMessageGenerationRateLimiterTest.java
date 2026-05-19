package com.example.meanhwa_back.message.ratelimit;

import java.time.Duration;

import com.example.meanhwa_back.message.config.MessageGenerationRateLimitProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** {@link InMemoryMessageGenerationRateLimiter} 고정 윈도우·사용자 격리 단위 테스트. */
class InMemoryMessageGenerationRateLimiterTest {
    private InMemoryMessageGenerationRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        MessageGenerationRateLimitProperties properties = new MessageGenerationRateLimitProperties();
        properties.setMaxRequests(2);
        properties.setWindow(Duration.ofHours(1));
        rateLimiter = new InMemoryMessageGenerationRateLimiter(properties);
    }

    @Test
    void allowsRequestsWithinLimit() {
        assertThat(rateLimiter.consume(1L).allowed()).isTrue();
        assertThat(rateLimiter.consume(1L).allowed()).isTrue();
    }

    @Test
    void blocksRequestsOverLimitUntilWindowExpires() {
        rateLimiter.consume(1L);
        rateLimiter.consume(1L);

        RateLimitDecision blocked = rateLimiter.consume(1L);
        assertThat(blocked.allowed()).isFalse();
        assertThat(blocked.remaining()).isZero();
    }

    @Test
    void isolatesLimitsPerUser() {
        rateLimiter.consume(1L);
        rateLimiter.consume(1L);
        rateLimiter.consume(1L);

        assertThat(rateLimiter.consume(2L).allowed()).isTrue();
    }
}
