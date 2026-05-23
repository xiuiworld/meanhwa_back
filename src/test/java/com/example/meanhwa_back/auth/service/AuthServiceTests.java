package com.example.meanhwa_back.auth.service;

import java.util.List;

import com.example.meanhwa_back.auth.dto.DevLoginRequest;
import com.example.meanhwa_back.common.error.BusinessException;
import com.example.meanhwa_back.common.error.ErrorCode;
import com.example.meanhwa_back.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AuthService의 프로필별 정책을 단위 수준에서 검증하는 테스트.
 * 운영 환경에서 개발용 로그인이 차단되는 안전장치를 고정한다.
 */
class AuthServiceTests {

    @Test
    void devLoginIsBlockedInProdProfile() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        AuthService authService = new AuthService(
                mock(UserRepository.class),
                mock(TokenService.class),
                environment,
                List.of(),
                mock(TransactionTemplate.class)
        );

        assertThatThrownBy(() -> authService.devLogin(new DevLoginRequest(
                "dev-user",
                "dev@example.com",
                "Dev User",
                "ROLE_USER"
        )))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER));
    }
}
