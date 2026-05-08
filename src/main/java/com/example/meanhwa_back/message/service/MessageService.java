package com.example.meanhwa_back.message.service;

import java.util.List;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.repository.FlowerRepository;
import com.example.meanhwa_back.message.dto.MessageGenerateRequest;
import com.example.meanhwa_back.message.dto.MessageGenerateResponse;
import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.repository.TagRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MessageService {
    private final FlowerRepository flowerRepository;
    private final TagRepository tagRepository;
    private final MessageGenerator messageGenerator;

    public MessageService(
            FlowerRepository flowerRepository,
            TagRepository tagRepository,
            MessageGenerator messageGenerator
    ) {
        this.flowerRepository = flowerRepository;
        this.tagRepository = tagRepository;
        this.messageGenerator = messageGenerator;
    }

    public MessageGenerateResponse generate(MessageGenerateRequest request) {
        Flower flower = flowerRepository.findById(request.flowerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FLOWER_NOT_FOUND));
        List<Long> selectedTagIds = normalizeTagIds(request.selectedTagIds());
        List<Tag> selectedTags = tagRepository.findAllById(selectedTagIds);
        if (selectedTags.size() != selectedTagIds.size()) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }

        String message = messageGenerator.generate(new MessageContext(
                flower,
                selectedTags,
                request.senderName().trim(),
                request.receiverName().trim()
        ));
        return new MessageGenerateResponse(flower.getId(), message);
    }

    private List<Long> normalizeTagIds(List<Long> tagIds) {
        if (tagIds == null) {
            return List.of();
        }

        return tagIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
    }
}
