package com.example.meanhwa_back.user.repository;

import java.util.Optional;

import com.example.meanhwa_back.auth.domain.OAuthProvider;
import com.example.meanhwa_back.user.domain.Role;
import com.example.meanhwa_back.user.domain.User;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 회원 조회와 관리자 검색을 담당하는 저장소.
 * 소셜 provider 식별자와 role 조건을 중심으로 인증, 백오피스, 통계 기능을 지원한다.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndOauthId(OAuthProvider provider, String oauthId);

    long countByRole(Role role);

    /**
     * 같은 회원 기준의 조회 후 쓰기 흐름을 직렬화하기 위해 회원 row에 쓰기 잠금을 건다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    /**
     * 백오피스 회원 목록 검색.
     * <p>{@code keyword}가 있으면 email·nickname·oauthId에 대소문자 무시 부분 일치.
     */
    @Query("""
            select u
            from User u
            where (:keyword is null or :keyword = ''
                or lower(u.email) like lower(concat('%', :keyword, '%'))
                or lower(u.nickname) like lower(concat('%', :keyword, '%'))
                or lower(u.oauthId) like lower(concat('%', :keyword, '%')))
              and (:role is null or u.role = :role)
              and (:provider is null or u.provider = :provider)
            """)
    Page<User> searchForAdmin(
            @Param("keyword") String keyword,
            @Param("role") Role role,
            @Param("provider") OAuthProvider provider,
            Pageable pageable
    );
}
