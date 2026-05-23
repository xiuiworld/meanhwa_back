package com.example.meanhwa_back.common.web;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 공통 page/size 정책이 모든 API에서 같은 경계값을 쓰도록 고정한다. */
class PageRequestUtilsTest {

    @Test
    void rejectsNegativePage() {
        assertThatThrownBy(() -> PageRequestUtils.normalizePage(-1))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST));
    }

    @Test
    void rejectsNonPositiveSize() {
        assertThatThrownBy(() -> PageRequestUtils.normalizeSize(0))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST));
    }

    @Test
    void capsOversizedRequestsAtOperationalMaximum() {
        PageRequest pageRequest = PageRequestUtils.of(2, 1000, Sort.by("createdAt"));

        assertThat(pageRequest.getPageNumber()).isEqualTo(2);
        assertThat(pageRequest.getPageSize()).isEqualTo(PageRequestUtils.MAX_SIZE);
    }
}
