package com.example.meanhwa_back.tag.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.domain.TagCategory;
import com.example.meanhwa_back.tag.dto.TagCategoryResponse;
import com.example.meanhwa_back.tag.dto.TagItemResponse;
import com.example.meanhwa_back.tag.repository.TagRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<TagCategoryResponse> getTagsByCategory() {
        Map<TagCategory, List<Tag>> tagsByCategory = tagRepository.findAllActive(Sort.by("category", "name"))
                .stream()
                .collect(Collectors.groupingBy(Tag::getCategory));

        return tagsByCategory.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new TagCategoryResponse(
                        entry.getKey(),
                        entry.getValue()
                                .stream()
                                .sorted(Comparator.comparing(Tag::getName))
                                .map(TagItemResponse::from)
                                .toList()
                ))
                .toList();
    }
}
