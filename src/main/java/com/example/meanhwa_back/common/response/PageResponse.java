package com.example.meanhwa_back.common.response;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;

/** 페이지네이션 결과 공통 형식. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) implements Serializable {
    /**
     * 도메인 객체나 스냅샷을 이 API 응답 DTO로 변환한다.
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
/**
 * 이미 계산된 페이지 메타데이터와 content로 PageResponse를 만든다.
 */

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page + 1 < totalPages;
        return new PageResponse<>(content, page, size, totalElements, totalPages, hasNext);
    }
}
