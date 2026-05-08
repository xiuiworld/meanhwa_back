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
                .map(history -> FlowerSummaryResponse.from(history.getFlower()))
                .toList();
    }

    @Transactional
    public void deleteHistories() {
        User user = authenticatedUserProvider.getCurrentUser();
        userHistoryRepository.deleteByUserId(user.getId());
    }

    @Transactional
    public void recordViewIfAuthenticated(Long flowerId) {
        User user = authenticatedUserProvider.getCurrentUserOrNull();
        if (user == null) {
            return;
        }

        Flower flower = flowerRepository.findById(flowerId)
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
