package com.example.meanhwa_back.user.controller;

import java.util.List;

import com.example.meanhwa_back.common.response.ApiResponse;
import com.example.meanhwa_back.flower.dto.FlowerSummaryResponse;
import com.example.meanhwa_back.user.dto.UserMeResponse;
import com.example.meanhwa_back.user.service.UserHistoryService;
import com.example.meanhwa_back.user.service.UserLikeService;
import com.example.meanhwa_back.user.service.UserService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 사용자 프로필·좋아요·조회 이력 API. */
@RestController
@RequestMapping("/api/v1/users/me")
public class UserController {
    private final UserService userService;
    private final UserLikeService userLikeService;
    private final UserHistoryService userHistoryService;

    public UserController(
            UserService userService,
            UserLikeService userLikeService,
            UserHistoryService userHistoryService
    ) {
        this.userService = userService;
        this.userLikeService = userLikeService;
        this.userHistoryService = userHistoryService;
    }

    @GetMapping
    public ApiResponse<UserMeResponse> getMe() {
        return ApiResponse.ok(userService.getMe());
    }

    @GetMapping("/likes")
    public ApiResponse<List<FlowerSummaryResponse>> getLikes() {
        return ApiResponse.ok(userLikeService.getLikes());
    }
/**
 * 인증 사용자의 꽃 찜을 추가하고 결과 표시용 꽃 요약을 반환한다.
 */

    @PostMapping("/likes/{flowerId}")
    public ApiResponse<FlowerSummaryResponse> addLike(@PathVariable Long flowerId) {
        return ApiResponse.ok(userLikeService.addLike(flowerId));
    }
/**
 * 인증 사용자의 꽃 찜을 해제한다.
 */

    @DeleteMapping("/likes/{flowerId}")
    public ApiResponse<Void> deleteLike(@PathVariable Long flowerId) {
        userLikeService.deleteLike(flowerId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/histories")
    public ApiResponse<List<FlowerSummaryResponse>> getHistories() {
        return ApiResponse.ok(userHistoryService.getHistories());
    }
/**
 * 인증 사용자의 최근 본 꽃 이력을 모두 삭제한다.
 */

    @DeleteMapping("/histories")
    public ApiResponse<Void> deleteHistories() {
        userHistoryService.deleteHistories();
        return ApiResponse.ok(null);
    }
}
