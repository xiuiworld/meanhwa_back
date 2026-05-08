package com.example.meanhwa_back.admin.service;

import com.example.meanhwa_back.admin.dto.AdminTagRequest;
import com.example.meanhwa_back.admin.dto.AdminTagResponse;
import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.repository.TagRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminTagService {
    private final TagRepository tagRepository;

    public AdminTagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "tags", allEntries = true),
            @CacheEvict(cacheNames = "flowers", allEntries = true)
    })
    public AdminTagResponse createTag(AdminTagRequest request) {
        String name = normalizeName(request.name());
        validateDuplicate(request.category(), name, null);
        return AdminTagResponse.from(tagRepository.save(new Tag(request.category(), name)));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "tags", allEntries = true),
            @CacheEvict(cacheNames = "flowers", allEntries = true)
    })
    public AdminTagResponse updateTag(Long tagId, AdminTagRequest request) {
        Tag tag = getActiveTag(tagId);
        String name = normalizeName(request.name());
        validateDuplicate(request.category(), name, tagId);
        tag.update(request.category(), name);
        return AdminTagResponse.from(tag);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "tags", allEntries = true),
            @CacheEvict(cacheNames = "flowers", allEntries = true)
    })
    public void deleteTag(Long tagId) {
        getActiveTag(tagId).softDelete();
    }

    private Tag getActiveTag(Long tagId) {
        return tagRepository.findActiveById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
    }

    private void validateDuplicate(com.example.meanhwa_back.tag.domain.TagCategory category, String name, Long excludedId) {
        if (tagRepository.existsActiveByCategoryAndName(category, name, excludedId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_TAG);
        }
    }

    private String normalizeName(String name) {
        return name.trim();
    }
}
