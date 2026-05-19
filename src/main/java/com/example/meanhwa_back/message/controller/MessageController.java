package com.example.meanhwa_back.message.controller;

import com.example.meanhwa_back.common.response.ApiResponse;
import com.example.meanhwa_back.message.dto.MessageGenerateRequest;
import com.example.meanhwa_back.message.dto.MessageGenerateResponse;
import com.example.meanhwa_back.message.service.MessageService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI/템플릿 기반 선물 메시지 생성 API.
 *
 * <p>{@code POST /generate}는 {@code Authorization: Bearer} 필수이며,
 * 사용자별 Rate Limit은 {@link com.example.meanhwa_back.message.service.MessageGenerationRateLimitService}에서 처리한다.
 */
@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** 로그인 사용자 기준 1시간당 생성 횟수 제한이 적용된다. */
    @PostMapping("/generate")
    public ApiResponse<MessageGenerateResponse> generate(@Valid @RequestBody MessageGenerateRequest request) {
        return ApiResponse.ok(messageService.generate(request));
    }
}
