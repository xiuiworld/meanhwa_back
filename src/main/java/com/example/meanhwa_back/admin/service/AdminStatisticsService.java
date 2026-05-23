package com.example.meanhwa_back.admin.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.example.meanhwa_back.admin.dto.statistics.DailyActiveUserResponse;
import com.example.meanhwa_back.admin.dto.statistics.PopularFlowerResponse;
import com.example.meanhwa_back.admin.dto.statistics.PopularTagResponse;
import com.example.meanhwa_back.admin.dto.statistics.StatisticsSummaryResponse;
import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.repository.FlowerRepository;
import com.example.meanhwa_back.log.domain.ActionType;
import com.example.meanhwa_back.log.repository.ActionLogRepository;
import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.repository.TagRepository;
import com.example.meanhwa_back.user.repository.UserLikeRepository;
import com.example.meanhwa_back.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 행동 로그·좋아요 등을 집계해 관리자 통계를 제공한다. */
@Service
@Transactional(readOnly = true)
public class AdminStatisticsService {
    private static final int DEFAULT_PERIOD_DAYS = 30;
    private static final int DEFAULT_LIMIT = 10;

    private final ActionLogRepository actionLogRepository;
    private final FlowerRepository flowerRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final UserLikeRepository userLikeRepository;
    private final ObjectMapper objectMapper;

    public AdminStatisticsService(
            ActionLogRepository actionLogRepository,
            FlowerRepository flowerRepository,
            TagRepository tagRepository,
            UserRepository userRepository,
            UserLikeRepository userLikeRepository,
            ObjectMapper objectMapper
    ) {
        this.actionLogRepository = actionLogRepository;
        this.flowerRepository = flowerRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.userLikeRepository = userLikeRepository;
        this.objectMapper = objectMapper;
    }

    public StatisticsSummaryResponse getSummary(LocalDate from, LocalDate to) {
        Period period = resolvePeriod(from, to);
        return new StatisticsSummaryResponse(
                period.fromDate(),
                period.toDate(),
                userRepository.count(),
                flowerRepository.countActive(),
                userLikeRepository.count(),
                actionLogRepository.countByActionTypeAndCreatedAtBetween(
                        ActionType.CURATION_START,
                        period.fromDateTime(),
                        period.toDateTime()
                ),
                actionLogRepository.countByActionTypeAndCreatedAtBetween(
                        ActionType.DICTIONARY_SEARCH,
                        period.fromDateTime(),
                        period.toDateTime()
                ),
                actionLogRepository.countByActionTypeAndCreatedAtBetween(
                        ActionType.FLOWER_DETAIL_VIEW,
                        period.fromDateTime(),
                        period.toDateTime()
                ),
                actionLogRepository.countByActionTypeAndCreatedAtBetween(
                        ActionType.CURATION_RESULT_CLICK,
                        period.fromDateTime(),
                        period.toDateTime()
                )
        );
    }

    public List<PopularTagResponse> getPopularTags(LocalDate from, LocalDate to, int limit) {
        Period period = resolvePeriod(from, to);
        Map<Long, Long> counts = new HashMap<>();
        for (ActionLogRepository.ActionDataView log : actionLogRepository.findActionDataByActionTypeInAndCreatedAtBetween(
                List.of(ActionType.CURATION_START),
                period.fromDateTime(),
                period.toDateTime()
        )) {
            List<Long> tagIds = readLongList(log.getActionData(), "tagIds");
            for (Long tagId : tagIds) {
                counts.merge(tagId, 1L, Long::sum);
            }
        }
        if (counts.isEmpty()) {
            return List.of();
        }

        Map<Long, Tag> tagsById = tagRepository.findActiveByIdIn(counts.keySet())
                .stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity()));

        return sortCounts(counts, limit).stream()
                .map(entry -> {
                    Tag tag = tagsById.get(entry.getKey());
                    return new PopularTagResponse(
                            entry.getKey(),
                            tag == null ? null : tag.getCategory(),
                            tag == null ? "삭제된 태그" : tag.getName(),
                            entry.getValue()
                    );
                })
                .toList();
    }

    public List<PopularFlowerResponse> getPopularFlowers(LocalDate from, LocalDate to, int limit) {
        Period period = resolvePeriod(from, to);
        Map<Long, Long> counts = new HashMap<>();
        for (ActionLogRepository.ActionDataView log : actionLogRepository.findActionDataByActionTypeInAndCreatedAtBetween(
                List.of(ActionType.FLOWER_DETAIL_VIEW, ActionType.CURATION_RESULT_CLICK),
                period.fromDateTime(),
                period.toDateTime()
        )) {
            Long flowerId = readLong(log.getActionData(), "flowerId");
            if (flowerId != null) {
                counts.merge(flowerId, 1L, Long::sum);
            }
        }
        if (counts.isEmpty()) {
            return List.of();
        }

        Map<Long, Flower> flowersById = flowerRepository.findActiveByIdIn(counts.keySet())
                .stream()
                .collect(Collectors.toMap(Flower::getId, Function.identity()));

        return sortCounts(counts, limit).stream()
                .map(entry -> {
                    Flower flower = flowersById.get(entry.getKey());
                    return new PopularFlowerResponse(
                            entry.getKey(),
                            flower == null ? "삭제된 꽃/식물" : flower.getName(),
                            flower == null ? null : flower.getImageUrl(),
                            entry.getValue()
                    );
                })
                .toList();
    }

    public List<DailyActiveUserResponse> getDailyActiveUsers(LocalDate from, LocalDate to) {
        Period period = resolvePeriod(from, to);
        Map<LocalDate, Set<Long>> usersByDate = new LinkedHashMap<>();
        LocalDate cursor = period.fromDate();
        while (!cursor.isAfter(period.toDate())) {
            usersByDate.put(cursor, new HashSet<>());
            cursor = cursor.plusDays(1);
        }

        for (ActionLogRepository.UserActivityView log :
                actionLogRepository.findUserActivityByCreatedAtBetween(period.fromDateTime(), period.toDateTime())) {
            usersByDate.computeIfAbsent(log.getCreatedAt().toLocalDate(), ignored -> new HashSet<>())
                    .add(log.getUserId());
        }

        return usersByDate.entrySet()
                .stream()
                .map(entry -> new DailyActiveUserResponse(entry.getKey(), entry.getValue().size()))
                .toList();
    }

    private Period resolvePeriod(LocalDate from, LocalDate to) {
        LocalDate resolvedTo = to == null ? LocalDate.now() : to;
        LocalDate resolvedFrom = from == null ? resolvedTo.minusDays(DEFAULT_PERIOD_DAYS - 1L) : from;
        if (resolvedFrom.isAfter(resolvedTo)) {
            throw new IllegalArgumentException("from은 to보다 이후일 수 없습니다.");
        }
        return new Period(
                resolvedFrom,
                resolvedTo,
                resolvedFrom.atStartOfDay(),
                resolvedTo.atTime(LocalTime.MAX)
        );
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1) {
            throw new IllegalArgumentException("limit은 1 이상이어야 합니다.");
        }
        return Math.min(limit, 100);
    }

    public int limitOrDefault(Integer limit) {
        return normalizeLimit(limit);
    }

    private List<Map.Entry<Long, Long>> sortCounts(Map<Long, Long> counts, int limit) {
        return counts.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .toList();
    }

    private Long readLong(String actionData, String fieldName) {
        Map<String, Object> data = readActionData(actionData);
        Object value = data.get(fieldName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private List<Long> readLongList(String actionData, String fieldName) {
        Map<String, Object> data = readActionData(actionData);
        Object value = data.get(fieldName);
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return values.stream()
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::longValue)
                .toList();
    }

    private Map<String, Object> readActionData(String actionData) {
        try {
            return objectMapper.readValue(actionData, new TypeReference<>() {
            });
        } catch (Exception exception) {
            return Map.of();
        }
    }

    private record Period(
            LocalDate fromDate,
            LocalDate toDate,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime
    ) {
    }
}
