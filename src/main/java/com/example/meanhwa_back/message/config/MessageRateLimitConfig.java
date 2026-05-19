package com.example.meanhwa_back.message.config;



import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.annotation.Configuration;



/**

 * {@link MessageGenerationRateLimitProperties} 바인딩 활성화.

 *

 * <p>실제 {@link com.example.meanhwa_back.message.ratelimit.MessageGenerationRateLimiter} 구현체는

 * {@code app.message.rate-limit.store} 값에 따라 컴포넌트 스캔 시 조건부 등록된다.

 */

@Configuration

@EnableConfigurationProperties(MessageGenerationRateLimitProperties.class)

public class MessageRateLimitConfig {

}

