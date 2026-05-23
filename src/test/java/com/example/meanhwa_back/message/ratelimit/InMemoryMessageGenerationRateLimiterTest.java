package com.example.meanhwa_back.message.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

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

    @Test
    void periodicallyEvictsExpiredUserWindows() {
        MessageGenerationRateLimitProperties properties = new MessageGenerationRateLimitProperties();
        properties.setMaxRequests(2);
        properties.setWindow(Duration.ofMillis(1));
        MutableClock clock = new MutableClock();
        InMemoryMessageGenerationRateLimiter limiter = new InMemoryMessageGenerationRateLimiter(properties, clock);

        limiter.consume(1L);
        clock.advance(Duration.ofMillis(2));
        for (long userId = 2L; userId <= 256L; userId++) {
            limiter.consume(userId);
        }

        assertThat(limiter.hasWindowForUser(1L)).isFalse();
    }

    private static class MutableClock extends Clock {
        private Instant instant = Instant.parse("2026-05-24T00:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
