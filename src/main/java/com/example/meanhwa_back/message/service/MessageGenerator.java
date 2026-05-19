package com.example.meanhwa_back.message.service;

/** 선물 메시지 생성 전략 (OpenAI / 템플릿). */
public interface MessageGenerator {
    String generate(MessageContext context);
}
