package com.example.meanhwa_back.message.service;

import java.util.List;

import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.tag.domain.Tag;
/**
 * 메시지 생성기에 전달하는 꽃, 태그, 발신자, 수신자 컨텍스트.
 * OpenAI 생성기와 템플릿 fallback이 같은 입력 모델을 공유하게 한다.
 */
public record MessageContext(
        Flower flower,
        List<Tag> selectedTags,
        String senderName,
        String receiverName
) {
}
