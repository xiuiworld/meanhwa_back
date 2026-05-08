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

    public CurationService(
            FlowerRepository flowerRepository,
            FlowerTagMappingRepository mappingRepository,
            TagRepository tagRepository
    ) {
        this.flowerRepository = flowerRepository;
        this.mappingRepository = mappingRepository;
        this.tagRepository = tagRepository;
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

        return toPage(results, page, size);
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
