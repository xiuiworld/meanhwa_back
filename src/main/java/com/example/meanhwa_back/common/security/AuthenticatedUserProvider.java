package com.example.meanhwa_back.common.security;

import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.user.domain.User;
import com.example.meanhwa_back.user.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** SecurityContext에서 현재 {@link User} 엔티티를 조회한다. */
@Component
public class AuthenticatedUserProvider {
    private final UserRepository userRepository;

    public AuthenticatedUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 인증된 principal의 userId로 현재 회원 엔티티를 조회한다.
     */
    public User getCurrentUser() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 현재 회원 row에 쓰기 잠금을 건 상태로 조회한다.
     * <p>같은 사용자의 중복 찜·조회 이력 저장처럼 조회 후 쓰기 흐름이 필요한 트랜잭션에서 사용한다.
     */
    public User getCurrentUserForUpdate() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userRepository.findByIdForUpdate(userDetails.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 인증 정보가 있으면 현재 회원을 조회하고, 익명 요청이면 {@code null}을 반환한다.
     */
    public User getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            return null;
        }
        return userRepository.findById(userDetails.getUserId()).orElse(null);
    }

    /**
     * 인증 정보가 있으면 현재 회원 row에 쓰기 잠금을 걸고, 익명 요청이면 {@code null}을 반환한다.
     */
    public User getCurrentUserOrNullForUpdate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            return null;
        }
        return userRepository.findByIdForUpdate(userDetails.getUserId()).orElse(null);
    }

    private CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userDetails;
    }
}
