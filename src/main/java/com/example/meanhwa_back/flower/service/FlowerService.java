package com.example.meanhwa_back.flower.service;

import java.util.List;
import java.util.Map;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.dto.FlowerDetailResponse;
import com.example.meanhwa_back.flower.dto.FlowerSummaryResponse;
import com.example.meanhwa_back.flower.dto.TagSummaryResponse;
import com.example.meanhwa_back.flower.repository.FlowerRepository;
import com.example.meanhwa_back.flower.repository.FlowerTagMappingRepository;
import com.example.meanhwa_back.log.domain.ActionType;
import com.example.meanhwa_back.log.service.ActionLogService;
import com.example.meanhwa_back.user.service.UserHistoryService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FlowerService {
    private final FlowerRepository flowerRepository;
    private final FlowerTagMappingRepository mappingRepository;
    private final UserHistoryService userHistoryService;
    private final ActionLogService actionLogService;

    public FlowerService(
            FlowerRepository flowerRepository,
            FlowerTagMappingRepository mappingRepository,
            UserHistoryService userHistoryService,
            ActionLogService actionLogService
    ) {
        this.flowerRepository = flowerRepository;
        this.mappingRepository = mappingRepository;
        this.userHistoryService = userHistoryService;
        this.actionLogService = actionLogService;
    }

    public PageResponse<FlowerSummaryResponse> searchFlowers(String keyword, int page, int size) {
        String normalizedKeyword = normalize(keyword);
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "name")
        );

        PageResponse<FlowerSummaryResponse> response = PageResponse.from(flowerRepository.search(normalizedKeyword, pageRequest)
                .map(FlowerSummaryResponse::from));
        if (normalizedKeyword != null) {
            actionLogService.record(ActionType.DICTIONARY_SEARCH, Map.of(
                    "keyword", normalizedKeyword,
                    "page", page,
                    "size", size,
                    "resultCount", response.content().size(),
                    "totalElements", response.totalElements()
            ));
        }
        return response;
    }

    @Transactional
    public FlowerDetailResponse getFlower(Long flowerId) {
        Flower flower = flowerRepository.findActiveById(flowerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLOWER_NOT_FOUND));
        List<TagSummaryResponse> tags = mappingRepository.findByFlowerIdWithTag(flowerId)
                .stream()
                .map(mapping -> TagSummaryResponse.from(mapping.getTag()))
                .toList();

        userHistoryService.recordViewIfAuthenticated(flowerId);
        actionLogService.record(ActionType.FLOWER_DETAIL_VIEW, Map.of("flowerId", flowerId));
        return FlowerDetailResponse.of(flower, tags);
    }

    private String normalize(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
