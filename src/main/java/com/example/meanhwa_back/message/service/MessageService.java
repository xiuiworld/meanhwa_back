package com.example.meanhwa_back.message.service;

import java.util.List;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.common.security.AuthenticatedUserProvider;
import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.repository.FlowerRepository;
import com.example.meanhwa_back.message.dto.MessageGenerateRequest;
import com.example.meanhwa_back.message.dto.MessageGenerateResponse;
import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.repository.TagRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 꽃·태그·수신자 정보를 바탕으로 선물 메시지를 생성한다.
 *
 * <p>로그인 사용자만 호출 가능하며, 사용자별 Rate Limit({@link MessageGenerationRateLimitService})이
 * OpenAI·템플릿 생성 전에 적용된다.
 */
@Service
@Transactional(readOnly = true)
public class MessageService {
    private final FlowerRepository flowerRepository;
    private final TagRepository tagRepository;
    private final MessageGenerator messageGenerator;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final MessageGenerationRateLimitService rateLimitService;

    public MessageService(
            FlowerRepository flowerRepository,
            TagRepository tagRepository,
            MessageGenerator messageGenerator,
            AuthenticatedUserProvider authenticatedUserProvider,
            MessageGenerationRateLimitService rateLimitService
    ) {
        this.flowerRepository = flowerRepository;
        this.tagRepository = tagRepository;
        this.messageGenerator = messageGenerator;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.rateLimitService = rateLimitService;
    }

    public MessageGenerateResponse generate(MessageGenerateRequest request) {
        Flower flower = flowerRepository.findActiveById(request.flowerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FLOWER_NOT_FOUND));
        List<Long> selectedTagIds = normalizeTagIds(request.selectedTagIds());
        List<Tag> selectedTags = tagRepository.findActiveByIdIn(selectedTagIds);
        if (selectedTags.size() != selectedTagIds.size()) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }

        // 유효한 생성 요청만 quota에 반영 (기본: 1시간당 10회 / userId)
        rateLimitService.checkAndConsume(authenticatedUserProvider.getCurrentUser().getId());

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
