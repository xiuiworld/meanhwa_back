package com.example.meanhwa_back.log.dto;

import java.util.List;

import com.example.meanhwa_back.curation.wizard.dto.CurationSelectionDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * {@code POST /api/v1/action-logs/curation-result-click} 요청 body.
 *
 * <p>큐레이션 <em>결과 목록에서 특정 꽃 카드를 탭했을 때</em> 프론트가 보내는 이벤트이다.
 * 서버 자동 {@code CURATION_START} 로그(목록 조회 성공, AOP)와 별개이다.
 *
 * <h2>위저드 v2 (P3 §9)</h2>
 * <ul>
 *   <li>{@link #source()} — {@code "curation-v2"} 권장</li>
 *   <li>{@link #selections()} — 결과 화면까지의 6단계 선택 스냅샷. 생략 가능(레거시 호환)</li>
 *   <li>{@link #flowVersion()} — 예: {@code 2026-05-v1}. 생략 가능</li>
 * </ul>
 */
public record CurationResultClickRequest(
        /** 클릭한 꽃 ID. */
        @NotNull Long flowerId,
        /** 당시 큐레이션 태그 ID. 없으면 저장 시 {@code []}. */
        List<Long> tagIds,
        /** 결과 리스트 순위(1부터). */
        @Min(1) Integer rank,
        /** 화면에 표시된 매칭 점수. */
        Integer score,
        /** 출처 구분. 위저드는 {@code curation-v2} 권장. */
        String source,
        /** 위저드 플로우 버전. {@code POST /curation/results} 와 동일 값 권장. */
        String flowVersion,
        /**
         * 6단계 선택 스냅샷 ({@code step}, {@code code}).
         * 레거시 화면에서는 보내지 않아도 된다.
         */
        @Valid List<CurationSelectionDto> selections
) {
}
