package com.example.meanhwa_back.flower.service;

import java.util.List;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.common.web.PageRequestUtils;
import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.ManagementLevel;
import com.example.meanhwa_back.flower.domain.PriceRange;
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
/**
 * 검색 필터를 정규화한 뒤 캐시 가능한 꽃 목록 페이지를 조회한다.
 */

    @LogAction(value = ActionType.DICTIONARY_SEARCH, extractor = DictionarySearchPayloadExtractor.class)
    public PageResponse<FlowerSummaryResponse> searchFlowers(
            String keyword,
            String priceRange,
            String isPetSafe,
            String managementLevel,
            List<Long> tagIds,
            int page,
            int size
    ) {
        String normalizedKeyword = normalize(keyword);
        PriceRange resolvedPriceRange = parsePriceRange(priceRange);
        Boolean resolvedIsPetSafe = parseBooleanFilter(isPetSafe);
        ManagementLevel resolvedManagementLevel = parseManagementLevel(managementLevel);
        List<Long> normalizedTagIds = normalizeTagIds(tagIds);
        int normalizedPage = PageRequestUtils.normalizePage(page);
        int normalizedSize = PageRequestUtils.normalizeSize(size);
        return flowerReadCacheService.searchFlowers(
                normalizedKeyword,
                resolvedPriceRange,
                resolvedIsPetSafe,
                resolvedManagementLevel,
                normalizedTagIds,
                normalizedPage,
                normalizedSize
        );
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

    private PriceRange parsePriceRange(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return PriceRange.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_FLOWER_FILTER);
        }
    }

    private ManagementLevel parseManagementLevel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ManagementLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_FLOWER_FILTER);
        }
    }

    private Boolean parseBooleanFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        if ("true".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized)) {
            return false;
        }
        throw new BusinessException(ErrorCode.INVALID_FLOWER_FILTER);
    }

    private List<Long> normalizeTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        if (tagIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BusinessException(ErrorCode.INVALID_FLOWER_FILTER);
        }
        List<Long> normalized = tagIds.stream()
                .distinct()
                .sorted()
                .toList();
        return normalized;
    }
}
