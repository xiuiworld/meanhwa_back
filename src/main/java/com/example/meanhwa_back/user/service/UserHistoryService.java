package com.example.meanhwa_back.user.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.common.security.AuthenticatedUserProvider;
import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.dto.FlowerSummaryResponse;
import com.example.meanhwa_back.flower.repository.FlowerRepository;
import com.example.meanhwa_back.user.domain.User;
import com.example.meanhwa_back.user.domain.UserHistory;
import com.example.meanhwa_back.user.repository.UserHistoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 꽃 상세 조회 이력 (최대 50건, 인증 시에만 기록). */
@Service
public class UserHistoryService {
    private static final int MAX_HISTORY_COUNT = 50;

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final FlowerRepository flowerRepository;
    private final UserHistoryRepository userHistoryRepository;

    public UserHistoryService(
            AuthenticatedUserProvider authenticatedUserProvider,
            FlowerRepository flowerRepository,
            UserHistoryRepository userHistoryRepository
    ) {
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.flowerRepository = flowerRepository;
        this.userHistoryRepository = userHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<FlowerSummaryResponse> getHistories() {
        User user = authenticatedUserProvider.getCurrentUser();
        return userHistoryRepository.findByUserIdOrderByViewedAtDesc(user.getId())
                .stream()
                .filter(history -> !history.getFlower().isDeleted())
                .map(history -> FlowerSummaryResponse.from(history.getFlower()))
                .toList();
    }

    /**
     * 인증 사용자의 최근 본 꽃 이력을 모두 삭제한다.
     */
    @Transactional
    public void deleteHistories() {
        User user = authenticatedUserProvider.getCurrentUser();
        userHistoryRepository.deleteByUserId(user.getId());
    }

    /**
     * 로그인 사용자인 경우 꽃 상세 조회 이력을 저장하거나 조회 시각을 갱신한다.
     */
    @Transactional
    public void recordViewIfAuthenticated(Long flowerId) {
        User user = authenticatedUserProvider.getCurrentUserOrNull();
        if (user == null) {
            return;
        }

        Flower flower = flowerRepository.findActiveById(flowerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLOWER_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();
        userHistoryRepository.findByUserIdAndFlowerId(user.getId(), flowerId)
                .ifPresentOrElse(
                        history -> history.updateViewedAt(now),
                        () -> userHistoryRepository.save(new UserHistory(user, flower, now))
                );
        deleteOverflow(user.getId());
    }

    private void deleteOverflow(Long userId) {
        long count = userHistoryRepository.countByUserId(userId);
        if (count <= MAX_HISTORY_COUNT) {
            return;
        }

        int overflow = (int) (count - MAX_HISTORY_COUNT);
        List<UserHistory> histories = userHistoryRepository.findByUserIdOrderByViewedAtAsc(userId);
        userHistoryRepository.deleteAll(histories.subList(0, overflow));
    }
}
