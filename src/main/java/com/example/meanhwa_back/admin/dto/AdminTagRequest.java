package com.example.meanhwa_back.admin.dto;

import com.example.meanhwa_back.tag.domain.TagCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 관리자 태그 생성과 수정 요청 본문.
 * 태그 카테고리와 이름을 검증해 중복 검사 이전에 명백한 입력 오류를 거른다.
 */
public record AdminTagRequest(
        @NotNull
        TagCategory category,

        @NotBlank
        @Size(max = 50)
        String name
) {
}
