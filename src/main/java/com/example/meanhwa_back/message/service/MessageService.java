package com.example.meanhwa_back.message.service;

import java.util.List;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.common.security.AuthenticatedUserProvider;
import com.example.meanhwa_back.common.web.PageRequestUtils;
import com.example.meanhwa_back.curation.history.domain.UserCurationResult;
import com.example.meanhwa_back.curation.history.service.CurationResultHistoryService;
import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.dto.TagSummaryResponse;
import com.example.meanhwa_back.flower.repository.FlowerRepository;
import com.example.meanhwa_back.message.domain.UserMessage;
import com.example.meanhwa_back.message.dto.MessageGenerateRequest;
import com.example.meanhwa_back.message.dto.MessageGenerateResponse;
import com.example.meanhwa_back.message.repository.UserMessageRepository;
import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.repository.TagRepository;
import com.example.meanhwa_back.user.domain.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private static final TypeReference<List<TagSummaryResponse>> TAG_LIST_TYPE = new TypeReference<>() {
    };

    private final FlowerRepository flowerRepository;
    private final TagRepository tagRepository;
    private final MessageGenerator messageGenerator;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final MessageGenerationRateLimitService rateLimitService;
    private final UserMessageRepository userMessageRepository;
    private final CurationResultHistoryService curationResultHistoryService;
    private final ObjectMapper objectMapper;

    public MessageService(
            FlowerRepository flowerRepository,
            TagRepository tagRepository,
            MessageGenerator messageGenerator,
            AuthenticatedUserProvider authenticatedUserProvider,
            MessageGenerationRateLimitService rateLimitService,
            UserMessageRepository userMessageRepository,
            CurationResultHistoryService curationResultHistoryService,
            ObjectMapper objectMapper
    ) {
        this.flowerRepository = flowerRepository;
        this.tagRepository = tagRepository;
        this.messageGenerator = messageGenerator;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.rateLimitService = rateLimitService;
        this.userMessageRepository = userMessageRepository;
        this.curationResultHistoryService = curationResultHistoryService;
        this.objectMapper = objectMapper;
    }
/**
 * 입력 컨텍스트를 바탕으로 선물 메시지를 생성한다.
 */

    @Transactional
    public MessageGenerateResponse generate(MessageGenerateRequest request) {
        User user = authenticatedUserProvider.getCurrentUser();
        Flower flower = flowerRepository.findActiveById(request.flowerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FLOWER_NOT_FOUND));
        List<Long> selectedTagIds = normalizeTagIds(request.selectedTagIds());
        List<Tag> selectedTags = tagRepository.findActiveByIdIn(selectedTagIds);
        if (selectedTags.size() != selectedTagIds.size()) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }

        UserCurationResult curationResult = curationResultHistoryService.getOwnedResultOrThrow(
                request.curationResultId(),
                user.getId()
        );

        // 유효한 생성 요청만 quota에 반영 (기본: 1시간당 10회 / userId)
        rateLimitService.checkAndConsume(user.getId());

        String senderName = request.senderName().trim();
        String receiverName = request.receiverName().trim();
        String message = messageGenerator.generate(new MessageContext(
                flower,
                selectedTags,
                senderName,
                receiverName
        ));

        List<TagSummaryResponse> selectedTagSnapshots = selectedTags.stream()
                .map(TagSummaryResponse::from)
                .toList();
        UserMessage saved = userMessageRepository.save(new UserMessage(
                user,
                flower,
                curationResult,
                flower.getName(),
                flower.getImageUrl(),
                flower.getCoreMeaning(),
                toJson(selectedTagSnapshots),
                senderName,
                receiverName,
                message
        ));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<MessageGenerateResponse> getMyMessages(int page, int size) {
        User user = authenticatedUserProvider.getCurrentUser();
        PageRequest pageRequest = PageRequestUtils.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(userMessageRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageRequest)
                .map(this::toResponse));
    }

    private MessageGenerateResponse toResponse(UserMessage message) {
        return new MessageGenerateResponse(
                message.getFlowerId(),
                message.getId(),
                message.getFlowerName(),
                message.getFlowerImageUrl(),
                message.getCoreMeaning(),
                fromJson(message.getSelectedTags()),
                message.getCurationResultId(),
                message.getSenderName(),
                message.getReceiverName(),
                message.getCreatedAt(),
                message.getMessage()
        );
    }

    private List<Long> normalizeTagIds(List<Long> tagIds) {
        if (tagIds == null) {
            return List.of();
        }
        if (tagIds.size() > 20 || tagIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "selectedTagIds는 1 이상의 숫자 최대 20개까지 허용됩니다.");
        }

        return tagIds.stream()
                .distinct()
                .toList();
    }

    private String toJson(List<TagSummaryResponse> tags) {
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private List<TagSummaryResponse> fromJson(String tags) {
        try {
            return objectMapper.readValue(tags, TAG_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
