package com.example.meanhwa_back.message.service;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.message.config.MessageGenerationRateLimitProperties;
import com.example.meanhwa_back.message.ratelimit.MessageGenerationRateLimiter;
import com.example.meanhwa_back.message.ratelimit.RateLimitDecision;

import org.springframework.stereotype.Service;

/**
 * 메시지 생성 API 진입 시 호출하는 Rate Limit 게이트.
 * <p>{@link MessageService#generate}에서 꽃·태그 검증이 끝난 뒤,
 * OpenAI/템플릿 생성 직전에 호출한다. 유효하지 않은 요청은 카운터에 포함하지 않는다.
 * <p>한도 초과 시 HTTP 429와 {@link ErrorCode#MESSAGE_GENERATION_RATE_LIMIT_EXCEEDED}를 반환한다.
 */
@Service
public class MessageGenerationRateLimitService {
    private final MessageGenerationRateLimitProperties properties;
    private final MessageGenerationRateLimiter rateLimiter;

    public MessageGenerationRateLimitService(
            MessageGenerationRateLimitProperties properties,
            MessageGenerationRateLimiter rateLimiter
    ) {
        this.properties = properties;
        this.rateLimiter = rateLimiter;
    }

    /**
     * 사용자 1회분 quota를 소비하고, 초과 시 예외를 던진다.
     *
     * @param userId JWT로 인증된 사용자 ID
     * @throws BusinessException {@code MESSAGE_GENERATION_RATE_LIMIT_EXCEEDED} (429)
     */
    public void checkAndConsume(Long userId) {
        if (!properties.isEnabled()) {
            return;
        }

        RateLimitDecision decision = rateLimiter.consume(userId);
        if (decision.allowed()) {
            return;
        }

        long windowMinutes = Math.max(1, properties.getWindow().toMinutes());
        long retryMinutes = Math.max(1, decision.retryAfter().toMinutes());
        throw new BusinessException(
                ErrorCode.MESSAGE_GENERATION_RATE_LIMIT_EXCEEDED,
                "메시지 생성은 %d분 동안 최대 %d회까지 가능합니다. 약 %d분 후에 다시 시도해 주세요."
                        .formatted(windowMinutes, decision.limit(), retryMinutes)
        );
    }
}
