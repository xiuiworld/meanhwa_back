package com.example.meanhwa_back.common.error;

/**
 * API 오류 응답의 공통 JSON 형태.
 * 상태 코드, 내부 오류 코드, 사용자 메시지를 일관된 필드명으로 전달한다.
 */
public record ErrorResponse(
        int status,
        String errorCode,
        String message
) {
    /**
     * ErrorCode 기본 메시지를 사용해 오류 응답을 만든다.
     */
    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }

    /**
     * 지정한 메시지를 사용해 오류 응답을 만든다.
     */
    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                message
        );
    }
}
