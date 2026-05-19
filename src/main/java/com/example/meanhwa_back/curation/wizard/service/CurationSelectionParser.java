package com.example.meanhwa_back.curation.wizard.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.curation.wizard.domain.CurationStepKey;
import com.example.meanhwa_back.curation.wizard.dto.CurationSelectionDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

/**
 * {@code GET .../options?selections=} 쿼리의 URL-encoded JSON 파싱.
 */
@Component
public class CurationSelectionParser {
    private static final TypeReference<List<CurationSelectionDto>> SELECTION_LIST_TYPE =
            new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public CurationSelectionParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<CurationStepKey, String> parseToMap(String selectionsJson) {
        if (selectionsJson == null || selectionsJson.isBlank()) {
            return Map.of();
        }
        String decoded = URLDecoder.decode(selectionsJson.trim(), StandardCharsets.UTF_8);
        List<CurationSelectionDto> selections = parseList(decoded);
        Map<CurationStepKey, String> map = new EnumMap<>(CurationStepKey.class);
        for (CurationSelectionDto selection : selections) {
            if (selection == null) {
                throw new BusinessException(ErrorCode.INVALID_CURATION_SELECTION);
            }
            CurationStepKey stepKey;
            try {
                stepKey = CurationStepKey.from(selection.step());
            } catch (IllegalArgumentException exception) {
                throw new BusinessException(ErrorCode.INVALID_CURATION_STEP);
            }
            String code = selection.code() == null ? "" : selection.code().trim().toUpperCase();
            if (code.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_CURATION_SELECTION);
            }
            if (map.containsKey(stepKey)) {
                throw new BusinessException(ErrorCode.INVALID_CURATION_SELECTION);
            }
            map.put(stepKey, code);
        }
        return map;
    }

    public List<CurationSelectionDto> parseList(String json) {
        try {
            List<CurationSelectionDto> parsed = objectMapper.readValue(json, SELECTION_LIST_TYPE);
            return parsed == null ? List.of() : parsed;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "selections JSON 형식이 올바르지 않습니다.");
        }
    }
}
