package com.example.meanhwa_back.admin.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.example.meanhwa_back.admin.dto.AdminFlowerRequest;
import com.example.meanhwa_back.admin.dto.FlowerTagMappingItemRequest;
import com.example.meanhwa_back.admin.dto.FlowerTagMappingUpdateRequest;
import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.FlowerTagMapping;
import com.example.meanhwa_back.flower.dto.FlowerDetailResponse;
import com.example.meanhwa_back.flower.dto.TagSummaryResponse;
import com.example.meanhwa_back.flower.repository.FlowerRepository;
import com.example.meanhwa_back.flower.repository.FlowerTagMappingRepository;
import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.repository.TagRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminFlowerService {
    private final FlowerRepository flowerRepository;
    private final FlowerTagMappingRepository mappingRepository;
    private final TagRepository tagRepository;

    public AdminFlowerService(
            FlowerRepository flowerRepository,
            FlowerTagMappingRepository mappingRepository,
            TagRepository tagRepository
    ) {
        this.flowerRepository = flowerRepository;
        this.mappingRepository = mappingRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public FlowerDetailResponse createFlower(AdminFlowerRequest request) {
        Flower flower = flowerRepository.save(new Flower(
                normalizeRequired(request.name()),
                normalizeOptional(request.imageUrl()),
                normalizeOptional(request.coreMeaning()),
                request.managementLevel(),
                normalizeOptional(request.managementInfo()),
                request.isToxicToPets(),
                request.priceRange()
        ));
        return toDetailResponse(flower);
    }

    @Transactional
    public FlowerDetailResponse updateFlower(Long flowerId, AdminFlowerRequest request) {
        Flower flower = getActiveFlower(flowerId);
        flower.update(
                normalizeRequired(request.name()),
                normalizeOptional(request.imageUrl()),
                normalizeOptional(request.coreMeaning()),
                request.managementLevel(),
                normalizeOptional(request.managementInfo()),
                request.isToxicToPets(),
                request.priceRange()
        );
        return toDetailResponse(flower);
    }

    @Transactional
    public void deleteFlower(Long flowerId) {
        getActiveFlower(flowerId).softDelete();
    }

    @Transactional
    public FlowerDetailResponse replaceMappings(Long flowerId, FlowerTagMappingUpdateRequest request) {
        Flower flower = getActiveFlower(flowerId);
        List<FlowerTagMappingItemRequest> items = request.tags();
        validateDuplicateTagIds(items);

        Map<Long, Tag> tagsById = tagRepository.findActiveByIdIn(items.stream()
                        .map(FlowerTagMappingItemRequest::tagId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity()));
        if (tagsById.size() != items.size()) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }

        mappingRepository.deleteByFlowerId(flowerId);
        mappingRepository.saveAll(items.stream()
                .map(item -> new FlowerTagMapping(flower, tagsById.get(item.tagId()), item.weight()))
                .toList());

        return toDetailResponse(flower);
    }

    private Flower getActiveFlower(Long flowerId) {
        return flowerRepository.findActiveById(flowerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLOWER_NOT_FOUND));
    }

    private FlowerDetailResponse toDetailResponse(Flower flower) {
        List<TagSummaryResponse> tags = mappingRepository.findByFlowerIdWithTag(flower.getId())
                .stream()
                .map(mapping -> TagSummaryResponse.from(mapping.getTag()))
                .toList();
        return FlowerDetailResponse.of(flower, tags);
    }

    private void validateDuplicateTagIds(List<FlowerTagMappingItemRequest> items) {
        HashSet<Long> tagIds = new HashSet<>();
        for (FlowerTagMappingItemRequest item : items) {
            if (item.tagId() == null || item.tagId() <= 0 || !tagIds.add(item.tagId())) {
                throw new BusinessException(ErrorCode.INVALID_MAPPING);
            }
        }
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
