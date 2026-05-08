package com.example.meanhwa_back.message.service;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class TemplateMessageGenerator implements MessageGenerator {

    @Override
    public String generate(MessageContext context) {
        String tagPhrase = context.selectedTags().isEmpty()
                ? "따뜻한 마음"
                : context.selectedTags()
                        .stream()
                        .map(tag -> tag.getName())
                        .collect(Collectors.joining(", "));

        return """
                %s님께,

                %s님이 %s의 마음을 담아 %s을(를) 전합니다.
                %s의 꽃말은 "%s"입니다.
                오늘의 마음이 오래 기억되는 선물이 되길 바랍니다.

                - %s 드림
                """.formatted(
                context.receiverName(),
                context.senderName(),
                tagPhrase,
                context.flower().getName(),
                context.flower().getName(),
                context.flower().getCoreMeaning(),
                context.senderName()
        ).trim();
    }
}
