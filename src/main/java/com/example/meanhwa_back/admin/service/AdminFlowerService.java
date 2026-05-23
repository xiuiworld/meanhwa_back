package com.example.meanhwa_back.admin.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.example.meanhwa_back.admin.dto.AdminFlowerDetailResponse;
import com.example.meanhwa_back.admin.dto.AdminFlowerRequest;
import com.example.meanhwa_back.admin.dto.AdminFlowerTagMappingResponse;
import com.example.meanhwa_back.admin.dto.FlowerTagMappingItemRequest;
import com.example.meanhwa_back.admin.dto.FlowerTagMappingUpdateRequest;
import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.common.security.AuthenticatedUserProvider;
import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.FlowerTagMapping;
import com.example.meanhwa_back.flower.repository.FlowerRepository;
import com.example.meanhwa_back.flower.repository.FlowerTagMappingRepository;
import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.repository.TagRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 꽃 등록·수정·삭제 및 태그 매핑 일괄 교체. */
@Service
public class AdminFlowerService {
    private final FlowerRepository flowerRepository;
    private final FlowerTagMappingRepository mappingRepository;
    private final TagRepository tagRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public AdminFlowerService(
            FlowerRepository flowerRepository,
            FlowerTagMappingRepository mappingRepository,
            TagRepository tagRepository,
            AuthenticatedUserProvider authenticatedUserProvider
    ) {
        this.flowerRepository = flowerRepository;
        this.mappingRepository = mappingRepository;
        this.tagRepository = tagRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }
/**
 * 관리자 요청을 검증한 뒤 새 꽃 데이터를 생성한다.
 */

    @Transactional
    @CacheEvict(cacheNames = "flowers", allEntries = true)
    public AdminFlowerDetailResponse createFlower(AdminFlowerRequest request) {
        Long adminUserId = currentAdminUserId();
        Flower flower = new Flower(
                normalizeRequired(request.name()),
                normalizeOptional(request.imageUrl()),
                normalizeOptional(request.coreMeaning()),
                normalizeOptional(request.description()),
                request.managementLevel(),
                request.isToxicToPets(),
                request.priceRange()
        );
        flower.markCreatedBy(adminUserId);
        flowerRepository.save(flower);
        return toDetailResponse(flower);
    }

    @Transactional(readOnly = true)
    public AdminFlowerDetailResponse getFlower(Long flowerId) {
        return toDetailResponse(getActiveFlower(flowerId));
    }
/**
 * 관리자 요청값으로 기존 꽃 데이터를 갱신한다.
 */

    @Transactional
    @CacheEvict(cacheNames = "flowers", allEntries = true)
    public AdminFlowerDetailResponse updateFlower(Long flowerId, AdminFlowerRequest request) {
        Long adminUserId = currentAdminUserId();
        Flower flower = getActiveFlower(flowerId);
        flower.update(
                normalizeRequired(request.name()),
                normalizeOptional(request.imageUrl()),
                normalizeOptional(request.coreMeaning()),
                normalizeOptional(request.description()),
                request.managementLevel(),
                request.isToxicToPets(),
                request.priceRange()
        );
        flower.markUpdatedBy(adminUserId);
        return toDetailResponse(flower);
    }
/**
 * 꽃을 물리 삭제하지 않고 soft delete 처리한다.
 */

    @Transactional
    @CacheEvict(cacheNames = "flowers", allEntries = true)
    public void deleteFlower(Long flowerId) {
        Flower flower = getActiveFlower(flowerId);
        flower.markUpdatedBy(currentAdminUserId());
        flower.softDelete();
    }
/**
 * 특정 꽃의 태그 매핑을 요청 목록 기준으로 일괄 교체한다.
 */

    @Transactional
    @CacheEvict(cacheNames = "flowers", allEntries = true)
    public AdminFlowerDetailResponse replaceMappings(Long flowerId, FlowerTagMappingUpdateRequest request) {
        Flower flower = getActiveFlower(flowerId);
        List<FlowerTagMappingItemRequest> items = request.tags();
        validateDuplicateTagIds(items);

        if (items.isEmpty()) {
            mappingRepository.deleteByFlowerId(flowerId);
            return toDetailResponse(flower);
        }

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

    private Long currentAdminUserId() {
        return authenticatedUserProvider.getCurrentUser().getId();
    }

    private AdminFlowerDetailResponse toDetailResponse(Flower flower) {
        List<AdminFlowerTagMappingResponse> tags = mappingRepository.findByFlowerIdWithTag(flower.getId())
                .stream()
                .map(AdminFlowerTagMappingResponse::from)
                .toList();
        return AdminFlowerDetailResponse.of(flower, tags);
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
