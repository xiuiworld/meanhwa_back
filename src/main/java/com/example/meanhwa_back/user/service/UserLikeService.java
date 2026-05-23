package com.example.meanhwa_back.user.service;

import java.util.List;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.common.security.AuthenticatedUserProvider;
import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.dto.FlowerSummaryResponse;
import com.example.meanhwa_back.flower.repository.FlowerRepository;
import com.example.meanhwa_back.user.domain.User;
import com.example.meanhwa_back.user.domain.UserLike;
import com.example.meanhwa_back.user.repository.UserLikeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 로그인 사용자의 꽃 좋아요 추가·삭제·목록 조회. */
@Service
public class UserLikeService {
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final FlowerRepository flowerRepository;
    private final UserLikeRepository userLikeRepository;

    public UserLikeService(
            AuthenticatedUserProvider authenticatedUserProvider,
            FlowerRepository flowerRepository,
            UserLikeRepository userLikeRepository
    ) {
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.flowerRepository = flowerRepository;
        this.userLikeRepository = userLikeRepository;
    }

    @Transactional(readOnly = true)
    public List<FlowerSummaryResponse> getLikes() {
        User user = authenticatedUserProvider.getCurrentUser();
        return userLikeRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .filter(userLike -> !userLike.getFlower().isDeleted())
                .map(userLike -> FlowerSummaryResponse.from(userLike.getFlower()))
                .toList();
    }
/**
 * 인증 사용자의 꽃 찜을 추가하고 결과 표시용 꽃 요약을 반환한다.
 */

    @Transactional
    public FlowerSummaryResponse addLike(Long flowerId) {
        User user = authenticatedUserProvider.getCurrentUser();
        Flower flower = flowerRepository.findActiveById(flowerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLOWER_NOT_FOUND));
        if (!userLikeRepository.existsByUserIdAndFlowerId(user.getId(), flowerId)) {
            userLikeRepository.save(new UserLike(user, flower));
        }
        return FlowerSummaryResponse.from(flower);
    }
/**
 * 인증 사용자의 꽃 찜을 해제한다.
 */

    @Transactional
    public void deleteLike(Long flowerId) {
        User user = authenticatedUserProvider.getCurrentUser();
        userLikeRepository.deleteByUserIdAndFlowerId(user.getId(), flowerId);
    }
}
