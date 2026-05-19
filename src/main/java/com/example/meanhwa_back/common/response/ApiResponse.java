package com.example.meanhwa_back.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 성공 응답 공통 래퍼 (status, message, data). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String message,
        T data
) {
    private static final String DEFAULT_SUCCESS_MESSAGE = "요청이 성공적으로 처리되었습니다.";

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, DEFAULT_SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, DEFAULT_SUCCESS_MESSAGE, data);
    }
}
