package com.example.meanhwa_back.flower.service;

import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.flower.dto.FlowerSummaryResponse;
import com.example.meanhwa_back.flower.repository.FlowerRepository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FlowerReadCacheService {
    private final FlowerRepository flowerRepository;

    public FlowerReadCacheService(FlowerRepository flowerRepository) {
        this.flowerRepository = flowerRepository;
    }

    @Cacheable(
            cacheNames = "flowers",
            key = "'search:' + (#keyword == null ? '' : #keyword) + ':page:' + #page + ':size:' + #size"
    )
    public PageResponse<FlowerSummaryResponse> searchFlowers(String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "name")
        );
        return PageResponse.from(flowerRepository.search(keyword, pageRequest)
                .map(FlowerSummaryResponse::from));
    }
}
