package com.example.meanhwa_back.curation.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.curation.dto.CurationFlowerResponse;
import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.FlowerTagMapping;
import com.example.meanhwa_back.flower.domain.PriceRange;
import com.example.meanhwa_back.flower.dto.TagSummaryResponse;
import com.example.meanhwa_back.flower.repository.FlowerRepository;
import com.example.meanhwa_back.flower.repository.FlowerTagMappingRepository;
import com.example.meanhwa_back.log.domain.ActionType;
import com.example.meanhwa_back.log.service.ActionLogService;
import com.example.meanhwa_back.tag.repository.TagRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CurationService {
    private final FlowerRepository flowerRepository;
    private final FlowerTagMappingRepository mappingRepository;
    private final TagRepository tagRepository;
    private final ActionLogService actionLogService;

    public CurationService(
            FlowerRepository flowerRepository,
            FlowerTagMappingRepository mappingRepository,
            TagRepository tagRepository,
            ActionLogService actionLogService
    ) {
        this.flowerRepository = flowerRepository;
        this.mappingRepository = mappingRepository;
        this.tagRepository = tagRepository;
        this.actionLogService = actionLogService;
    }

    public PageResponse<CurationFlowerResponse> curate(
            List<Long> tagIds,
            Boolean isPetSafe,
            String priceRangeValue,
            int page,
            int size
    ) {
        PriceRange priceRange = PriceRange.from(priceRangeValue);
        List<Long> normalizedTagIds = normalizeTagIds(tagIds);
        validateTagIds(normalizedTagIds);

        List<CurationFlowerResponse> results = normalizedTagIds.isEmpty()
                ? scoreAllFlowers(isPetSafe, priceRange)
                : scoreMatchedFlowers(normalizedTagIds, isPetSafe, priceRange);

        PageResponse<CurationFlowerResponse> response = toPage(results, page, size);
        recordCurationLog(normalizedTagIds, isPetSafe, priceRange, page, size, response);
        return response;
    }

    private List<CurationFlowerResponse> scoreMatchedFlowers(
            List<Long> tagIds,
            Boolean isPetSafe,
            PriceRange priceRange
    ) {
        Map<Long, CurationAccumulator> accumulators = new LinkedHashMap<>();

        for (FlowerTagMapping mapping : mappingRepository.findByTagIdsWithFlowerAndTag(tagIds)) {
            Flower flower = mapping.getFlower();
            if (!matchesFilters(flower, isPetSafe, priceRange)) {
                continue;
            }

            CurationAccumulator accumulator = accumulators.computeIfAbsent(
                    flower.getId(),
                    id -> new CurationAccumulator(flower)
            );
            accumulator.add(mapping);
        }

        return accumulators.values()
                .stream()
                .map(CurationAccumulator::toResponse)
                .sorted(curationComparator())
                .toList();
    }

    private List<CurationFlowerResponse> scoreAllFlowers(Boolean isPetSafe, PriceRange priceRange) {
        return flowerRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .filter(flower -> matchesFilters(flower, isPetSafe, priceRange))
                .map(flower -> CurationFlowerResponse.of(flower, 0, List.of()))
                .toList();
    }

    private boolean matchesFilters(Flower flower, Boolean isPetSafe, PriceRange priceRange) {
        if (Boolean.TRUE.equals(isPetSafe) && flower.isToxicToPets()) {
            return false;
        }
        return priceRange == null || flower.getPriceRange() == priceRange;
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

    private void validateTagIds(List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return;
        }

        long foundCount = tagRepository.countByIdIn(tagIds);
        if (foundCount != tagIds.size()) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }
    }

    private PageResponse<CurationFlowerResponse> toPage(List<CurationFlowerResponse> results, int page, int size) {
        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("page는 0 이상, size는 1 이상이어야 합니다.");
        }

        int fromIndex = Math.min(page * size, results.size());
        int toIndex = Math.min(fromIndex + size, results.size());
        return PageResponse.of(results.subList(fromIndex, toIndex), page, size, results.size());
    }

    private Comparator<CurationFlowerResponse> curationComparator() {
        return Comparator
                .comparing(CurationFlowerResponse::score, Comparator.reverseOrder())
                .thenComparing(CurationFlowerResponse::name);
    }

    private void recordCurationLog(
            List<Long> tagIds,
            Boolean isPetSafe,
            PriceRange priceRange,
            int page,
            int size,
            PageResponse<CurationFlowerResponse> response
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tagIds", tagIds);
        payload.put("isPetSafe", isPetSafe);
        payload.put("priceRange", priceRange == null ? null : priceRange.name());
        payload.put("page", page);
        payload.put("size", size);
        payload.put("resultCount", response.content().size());
        payload.put("totalElements", response.totalElements());
        payload.put("resultFlowerIds", response.content()
                .stream()
                .map(CurationFlowerResponse::flowerId)
                .toList());
        actionLogService.record(ActionType.CURATION_START, payload);
    }

    private static class CurationAccumulator {
        private final Flower flower;
        private final List<TagSummaryResponse> matchedTags = new ArrayList<>();
        private int score;

        CurationAccumulator(Flower flower) {
            this.flower = flower;
        }

        void add(FlowerTagMapping mapping) {
            this.score += mapping.getWeight();
            this.matchedTags.add(TagSummaryResponse.from(mapping.getTag()));
        }

        CurationFlowerResponse toResponse() {
            List<TagSummaryResponse> sortedTags = matchedTags.stream()
                    .sorted(Comparator.comparing(TagSummaryResponse::category).thenComparing(TagSummaryResponse::name))
                    .toList();
            return CurationFlowerResponse.of(flower, score, sortedTags);
        }
    }
}
