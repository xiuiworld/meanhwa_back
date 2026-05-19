package com.example.meanhwa_back.message.config;



import java.time.Duration;



import org.springframework.boot.context.properties.ConfigurationProperties;



/**
 * 메시지 생성 Rate Limit 설정.
 * <p>application.yml 예시:
 * <pre>
 * app:
 *   message:
 *     rate-limit:
 *       enabled: true
 *       max-requests: 10
 *       window: 1h
 *       store: memory   # prod: redis
 * </pre>

 */

@ConfigurationProperties(prefix = "app.message.rate-limit")

public class MessageGenerationRateLimitProperties {

    /** false이면 {@link com.example.meanhwa_back.message.service.MessageGenerationRateLimitService}가 검사를 건너뛴다. */

    private boolean enabled = true;



    /** 고정 윈도우 안에서 허용할 최대 생성 횟수 (기본 10). */

    private int maxRequests = 10;



    /** 윈도우 길이. 첫 요청 시점부터 이 시간이 지나면 카운터가 리셋된다 (기본 1시간). */

    private Duration window = Duration.ofHours(1);



    /** 카운터 저장소. 로컬/테스트는 MEMORY, 운영은 REDIS 권장. */

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

    /** Rate Limit 카운터를 어디에 보관할지 정의한다. */

    public enum Store {

        /** 단일 JVM 메모리 (로컬·테스트, 인스턴스마다 카운터 분리). */

        MEMORY,

        /** Redis (운영, 다중 인스턴스 간 quota 공유). */

        REDIS

    }

}

