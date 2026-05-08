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

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/generate")
    public ApiResponse<MessageGenerateResponse> generate(@Valid @RequestBody MessageGenerateRequest request) {
        return ApiResponse.ok(messageService.generate(request));
    }
}
