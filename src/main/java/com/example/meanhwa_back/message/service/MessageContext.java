package com.example.meanhwa_back.message.service;

import java.util.List;

import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.tag.domain.Tag;

public record MessageContext(
        Flower flower,
        List<Tag> selectedTags,
        String senderName,
        String receiverName
) {
}
