package com.example.meanhwa_back.common.web;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * API 전역의 page/size 정책을 한 곳에서 적용한다.
 * <p>음수 page와 1 미만 size는 명확한 400 오류로 돌려보내고, 과도한 size는 운영 보호를 위해 상한으로 줄인다.
 */
public final class PageRequestUtils {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private PageRequestUtils() {
    }
/**
 * page 요청값을 검증하고 허용되는 0 기반 페이지 번호로 반환한다.
 */

    public static int normalizePage(int page) {
        if (page < 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "page는 0 이상이어야 합니다.");
        }
        return page;
    }
/**
 * size 요청값을 검증하고 운영 보호용 최대 크기 안으로 제한한다.
 */

    public static int normalizeSize(int size) {
        if (size < 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "size는 1 이상이어야 합니다.");
        }
        return Math.min(size, MAX_SIZE);
    }
/**
 * 도메인 값들을 조합해 이 API 응답 DTO를 만든다.
 */

    public static PageRequest of(int page, int size, Sort sort) {
        return PageRequest.of(normalizePage(page), normalizeSize(size), sort);
    }
}
