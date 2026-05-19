package com.example.meanhwa_back.flower.service;

import java.util.List;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.dto.FlowerDetailResponse;
import com.example.meanhwa_back.flower.dto.FlowerSummaryResponse;
import com.example.meanhwa_back.flower.dto.TagSummaryResponse;
import com.example.meanhwa_back.flower.repository.FlowerRepository;
import com.example.meanhwa_back.flower.repository.FlowerTagMappingRepository;
import com.example.meanhwa_back.log.aop.LogAction;
import com.example.meanhwa_back.log.aop.extractor.DictionarySearchPayloadExtractor;
import com.example.meanhwa_back.log.aop.extractor.FlowerDetailViewPayloadExtractor;
import com.example.meanhwa_back.log.domain.ActionType;
import com.example.meanhwa_back.user.service.UserHistoryService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 꽃 검색·상세 조회 및 조회 이력·행동 로그 기록. */
@Service
@Transactional(readOnly = true)
public class FlowerService {
    private final FlowerRepository flowerRepository;
    private final FlowerTagMappingRepository mappingRepository;
    private final UserHistoryService userHistoryService;
    private final FlowerReadCacheService flowerReadCacheService;

    public FlowerService(
            FlowerRepository flowerRepository,
            FlowerTagMappingRepository mappingRepository,
            UserHistoryService userHistoryService,
            FlowerReadCacheService flowerReadCacheService
    ) {
        this.flowerRepository = flowerRepository;
        this.mappingRepository = mappingRepository;
        this.userHistoryService = userHistoryService;
        this.flowerReadCacheService = flowerReadCacheService;
    }

    @LogAction(value = ActionType.DICTIONARY_SEARCH, extractor = DictionarySearchPayloadExtractor.class)
    public PageResponse<FlowerSummaryResponse> searchFlowers(String keyword, int page, int size) {
        String normalizedKeyword = normalize(keyword);
        return flowerReadCacheService.searchFlowers(normalizedKeyword, page, size);
    }

    @LogAction(value = ActionType.FLOWER_DETAIL_VIEW, extractor = FlowerDetailViewPayloadExtractor.class)
    @Transactional
    public FlowerDetailResponse getFlower(Long flowerId) {
        Flower flower = flowerRepository.findActiveById(flowerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLOWER_NOT_FOUND));
        List<TagSummaryResponse> tags = mappingRepository.findByFlowerIdWithTag(flowerId)
                .stream()
                .map(mapping -> TagSummaryResponse.from(mapping.getTag()))
                .toList();

        userHistoryService.recordViewIfAuthenticated(flowerId);
        return FlowerDetailResponse.of(flower, tags);
    }

    private String normalize(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
