package com.example.meanhwa_back.common.error;

import org.springframework.http.HttpStatus;

/** API 오류 응답에 사용하는 HTTP 상태·코드·기본 메시지 정의. */
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청값이 올바르지 않습니다."),
    INVALID_PRICE_RANGE(HttpStatus.BAD_REQUEST, "INVALID_PRICE_RANGE", "유효하지 않은 예산 범위입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "UNSUPPORTED_OAUTH_PROVIDER", "지원하지 않는 OAuth provider입니다."),
    INVALID_OAUTH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_OAUTH_TOKEN", "유효하지 않은 OAuth 토큰입니다."),
    FLOWER_NOT_FOUND(HttpStatus.NOT_FOUND, "FLOWER_NOT_FOUND", "꽃/식물 정보를 찾을 수 없습니다."),
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "TAG_NOT_FOUND", "태그 정보를 찾을 수 없습니다."),
    CURATION_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "CURATION_RESULT_NOT_FOUND", "큐레이션 결과를 찾을 수 없습니다."),
    INVALID_FLOWER_FILTER(HttpStatus.BAD_REQUEST, "INVALID_FLOWER_FILTER", "꽃 도감 필터가 올바르지 않습니다."),
    /** 위저드 step key가 enum에 없을 때. */
    INVALID_CURATION_STEP(HttpStatus.BAD_REQUEST, "INVALID_CURATION_STEP", "큐레이션 단계가 올바르지 않습니다."),
    /** 분기표에 없는 code 조합·허용되지 않는 option code. */
    INVALID_CURATION_SELECTION(HttpStatus.BAD_REQUEST, "INVALID_CURATION_SELECTION", "큐레이션 선택이 올바르지 않습니다."),
    /** POST /results 시 6단계 미완료·step 누락. */
    INCOMPLETE_CURATION_SELECTION(HttpStatus.BAD_REQUEST, "INCOMPLETE_CURATION_SELECTION", "큐레이션 선택이 완료되지 않았습니다."),
    /** 요청 flowVersion이 서버에 없을 때. */
    CURATION_FLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "CURATION_FLOW_NOT_FOUND", "큐레이션 플로우를 찾을 수 없습니다."),
    DUPLICATE_TAG(HttpStatus.CONFLICT, "DUPLICATE_TAG", "이미 존재하는 태그입니다."),
    INVALID_MAPPING(HttpStatus.BAD_REQUEST, "INVALID_MAPPING", "꽃-태그 매핑 요청이 올바르지 않습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "지원하지 않는 이미지 형식입니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE", "업로드 가능한 파일 크기를 초과했습니다."),
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_FAILED", "이미지 업로드에 실패했습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "회원 정보를 찾을 수 없습니다."),
    /** 관리자가 자신의 role을 백오피스 API로 변경하려 할 때. */
    CANNOT_CHANGE_OWN_ROLE(HttpStatus.BAD_REQUEST, "CANNOT_CHANGE_OWN_ROLE", "본인 계정의 권한은 이 API로 변경할 수 없습니다."),
    /** 시스템에 남은 마지막 ROLE_ADMIN을 일반 사용자로 내릴 수 없을 때. */
    LAST_ADMIN_CANNOT_BE_DEMOTED(
            HttpStatus.BAD_REQUEST,
            "LAST_ADMIN_CANNOT_BE_DEMOTED",
            "마지막 관리자 계정의 권한은 변경할 수 없습니다."
    ),
    /** 사용자별 메시지 생성 고정 윈도우(기본 1시간) quota 초과. 상세 안내는 예외 message 필드 참고. */
    MESSAGE_GENERATION_RATE_LIMIT_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS,
            "MESSAGE_GENERATION_RATE_LIMIT_EXCEEDED",
            "메시지 생성 횟수를 초과했습니다."
    ),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
