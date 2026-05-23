package com.example.meanhwa_back.message.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 메시지 생성 Rate Limit 설정.
 * <p>로컬·테스트는 메모리 저장소를 쓰고, 운영은 Redis 저장소로 여러 인스턴스 간 quota를 공유한다.
 */
@Validated
@ConfigurationProperties(prefix = "app.message.rate-limit")
public class MessageGenerationRateLimitProperties {
    /** false이면 {@link com.example.meanhwa_back.message.service.MessageGenerationRateLimitService}가 검사를 건너뛴다. */
    private boolean enabled = true;

    /** 고정 윈도우 안에서 허용할 최대 생성 횟수. */
    @Min(1)
    private int maxRequests = 10;

    /** 첫 요청 시점부터 카운터가 유지되는 고정 윈도우 길이. */
    @NotNull
    private Duration window = Duration.ofHours(1);

    /** 카운터 저장소. */
    @NotNull
    private Store store = Store.MEMORY;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public Duration getWindow() {
        return window;
    }

    public void setWindow(Duration window) {
        this.window = window;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    @AssertTrue(message = "Rate limit window must be positive.")
    public boolean isWindowPositive() {
        return window != null && !window.isZero() && !window.isNegative();
    }

    /** Rate Limit 카운터를 어디에 보관할지 정의한다. */
    public enum Store {
        /** 단일 JVM 메모리. 로컬·테스트에서 사용하며 인스턴스마다 카운터가 분리된다. */
        MEMORY,

        /** Redis. 운영에서 사용하며 다중 인스턴스 간 quota를 공유한다. */
        REDIS
    }
}
