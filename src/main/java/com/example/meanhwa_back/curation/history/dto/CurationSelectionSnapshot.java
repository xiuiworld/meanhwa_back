package com.example.meanhwa_back.curation.history.dto;
/**
 * 큐레이션 저장 시점의 사용자의 단계별 선택 스냅샷.
 * flowVersion과 함께 저장되어 이후 플로우 정의가 바뀌어도 과거 선택을 해석할 수 있다.
 */

public record CurationSelectionSnapshot(
        String step,
        String code,
        String label
) {
}
