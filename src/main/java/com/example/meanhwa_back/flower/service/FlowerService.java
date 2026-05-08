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

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FlowerService {
    private final FlowerRepository flowerRepository;
    private final FlowerTagMappingRepository mappingRepository;

    public FlowerService(FlowerRepository flowerRepository, FlowerTagMappingRepository mappingRepository) {
        this.flowerRepository = flowerRepository;
        this.mappingRepository = mappingRepository;
    }

    public PageResponse<FlowerSummaryResponse> searchFlowers(String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "name")
        );

        return PageResponse.from(flowerRepository.search(normalize(keyword), pageRequest)
                .map(FlowerSummaryResponse::from));
    }

    public FlowerDetailResponse getFlower(Long flowerId) {
        Flower flower = flowerRepository.findById(flowerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLOWER_NOT_FOUND));
        List<TagSummaryResponse> tags = mappingRepository.findByFlowerIdWithTag(flowerId)
                .stream()
                .map(mapping -> TagSummaryResponse.from(mapping.getTag()))
                .toList();

        return FlowerDetailResponse.of(flower, tags);
    }

    private String normalize(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
