package com.example.meanhwa_back.tag.controller;

import java.util.List;

import com.example.meanhwa_back.common.response.ApiResponse;
import com.example.meanhwa_back.tag.dto.TagCategoryResponse;
import com.example.meanhwa_back.tag.service.TagService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ApiResponse<List<TagCategoryResponse>> getTags() {
        return ApiResponse.ok(tagService.getTagsByCategory());
    }
}
