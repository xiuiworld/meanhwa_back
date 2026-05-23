package com.example.meanhwa_back.curation.wizard.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.curation.wizard.domain.CurationBudgetCode;
import com.example.meanhwa_back.flower.domain.PriceRange;
import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.repository.TagRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 위저드 {@code code} ↔ DB {@code tags.id} 변환.
 * <p>P2에서 점수 합산 전에 모든 scoring code가 태그로 존재하는지 검증한다.
 * {@link CurationBudgetCode}는 태그가 아니라 {@link PriceRange} 필터로만 처리한다.
 */
@Component
@Transactional(readOnly = true)
public class CurationCodeResolver {
    private final TagRepository tagRepository;

    public CurationCodeResolver(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * Step1~5 code를 tagId 목록으로 변환. 하나라도 DB에 없으면 {@link ErrorCode#TAG_NOT_FOUND}.
     */
    public List<Long> resolveScoringTagIds(Set<String> scoringCodes) {
        if (scoringCodes.isEmpty()) {
            return List.of();
        }
        Map<String, Long> tagIdByCode = loadTagIds(scoringCodes);
        for (String code : scoringCodes) {
            if (!tagIdByCode.containsKey(code)) {
                throw new BusinessException(
                        ErrorCode.TAG_NOT_FOUND,
                        "태그 code가 DB에 없습니다: " + code
                );
            }
        }
        return scoringCodes.stream().map(tagIdByCode::get).distinct().toList();
    }

    /**
     * 큐레이션 선택값에서 예산 단계를 찾아 꽃 가격대 필터로 변환한다.
     */
    public PriceRange resolveBudgetPriceRange(String budgetCode) {
        return CurationBudgetCode.fromCode(budgetCode)
                .map(CurationBudgetCode::getPriceRange)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CURATION_SELECTION));
    }

    private Map<String, Long> loadTagIds(Collection<String> codes) {
        List<Tag> tags = tagRepository.findActiveByCodeIn(codes);
        Map<String, Long> map = new HashMap<>();
        for (Tag tag : tags) {
            if (tag.getCode() != null) {
                map.put(tag.getCode().toUpperCase(), tag.getId());
            }
        }
        return map;
    }
}
