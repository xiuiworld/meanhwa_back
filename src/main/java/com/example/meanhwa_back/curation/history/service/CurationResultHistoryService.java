package com.example.meanhwa_back.curation.history.service;

import java.util.List;
import java.util.stream.IntStream;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.common.response.PageResponse;
import com.example.meanhwa_back.common.security.AuthenticatedUserProvider;
import com.example.meanhwa_back.curation.dto.CurationFlowerResponse;
import com.example.meanhwa_back.curation.history.domain.UserCurationResult;
import com.example.meanhwa_back.curation.history.dto.CurationRecommendationSnapshot;
import com.example.meanhwa_back.curation.history.dto.CurationResultDetailResponse;
import com.example.meanhwa_back.curation.history.dto.CurationResultSummaryResponse;
import com.example.meanhwa_back.curation.history.dto.CurationSelectionSnapshot;
import com.example.meanhwa_back.curation.history.dto.CurationTopFlowerResponse;
import com.example.meanhwa_back.curation.history.repository.UserCurationResultRepository;
import com.example.meanhwa_back.user.domain.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 로그인 사용자의 큐레이션 결과 저장·조회. */
@Service
public class CurationResultHistoryService {
    private static final TypeReference<List<CurationSelectionSnapshot>> SELECTION_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<CurationRecommendationSnapshot>> RECOMMENDATION_LIST_TYPE =
            new TypeReference<>() {
            };

    private final UserCurationResultRepository curationResultRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ObjectMapper objectMapper;

    public CurationResultHistoryService(
            UserCurationResultRepository curationResultRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            ObjectMapper objectMapper
    ) {
        this.curationResultRepository = curationResultRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UserCurationResult saveIfAuthenticated(
            String flowVersion,
            List<CurationSelectionSnapshot> selections,
            PageResponse<CurationFlowerResponse> page
    ) {
        User user = authenticatedUserProvider.getCurrentUserOrNull();
        if (user == null) {
            return null;
        }

        List<CurationRecommendationSnapshot> recommendations = toRecommendations(page.content());
        return curationResultRepository.save(new UserCurationResult(
                user,
                flowVersion,
                toJson(selections),
                toJson(recommendations),
                Math.toIntExact(page.totalElements())
        ));
    }

    @Transactional(readOnly = true)
    public PageResponse<CurationResultSummaryResponse> getMyResults(int page, int size) {
        User user = authenticatedUserProvider.getCurrentUser();
        PageRequest pageRequest = pageRequest(page, size);
        return PageResponse.from(curationResultRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageRequest)
                .map(this::toSummaryResponse));
    }

    @Transactional(readOnly = true)
    public CurationResultDetailResponse getLatestResult() {
        User user = authenticatedUserProvider.getCurrentUser();
        UserCurationResult result = curationResultRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CURATION_RESULT_NOT_FOUND));
        return toDetailResponse(result);
    }

    @Transactional(readOnly = true)
    public CurationResultDetailResponse getResult(Long resultId) {
        User user = authenticatedUserProvider.getCurrentUser();
        UserCurationResult result = curationResultRepository.findByIdAndUserId(resultId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CURATION_RESULT_NOT_FOUND));
        return toDetailResponse(result);
    }

    @Transactional(readOnly = true)
    public UserCurationResult getOwnedResultOrThrow(Long resultId, Long userId) {
        if (resultId == null) {
            return null;
        }
        return curationResultRepository.findByIdAndUserId(resultId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CURATION_RESULT_NOT_FOUND));
    }

    private CurationResultSummaryResponse toSummaryResponse(UserCurationResult result) {
        List<CurationRecommendationSnapshot> recommendations = parseRecommendations(result);
        List<CurationTopFlowerResponse> topFlowers = recommendations.stream()
                .limit(3)
                .map(CurationTopFlowerResponse::from)
                .toList();
        return new CurationResultSummaryResponse(
                result.getId(),
                result.getFlowVersion(),
                parseSelections(result),
                topFlowers,
                result.getResultCount(),
                result.getCreatedAt()
        );
    }

    private CurationResultDetailResponse toDetailResponse(UserCurationResult result) {
        return new CurationResultDetailResponse(
                result.getId(),
                result.getFlowVersion(),
                parseSelections(result),
                parseRecommendations(result),
                result.getCreatedAt()
        );
    }

    private List<CurationRecommendationSnapshot> toRecommendations(List<CurationFlowerResponse> flowers) {
        return IntStream.range(0, flowers.size())
                .mapToObj(index -> {
                    CurationFlowerResponse flower = flowers.get(index);
                    return new CurationRecommendationSnapshot(
                            index + 1,
                            flower.flowerId(),
                            flower.name(),
                            flower.imageUrl(),
                            flower.coreMeaning(),
                            flower.priceRange(),
                            flower.isPetSafe(),
                            flower.score(),
                            flower.recommendationReason(),
                            flower.matchedTags()
                    );
                })
                .toList();
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(
                Math.max(page, 0),
                size < 1 ? 20 : Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    private List<CurationSelectionSnapshot> parseSelections(UserCurationResult result) {
        return fromJson(result.getSelections(), SELECTION_LIST_TYPE);
    }

    private List<CurationRecommendationSnapshot> parseRecommendations(UserCurationResult result) {
        return fromJson(result.getRecommendations(), RECOMMENDATION_LIST_TYPE);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private <T> T fromJson(String value, TypeReference<T> type) {
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
