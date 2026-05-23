package com.example.meanhwa_back.user.domain;

import java.time.LocalDateTime;

import com.example.meanhwa_back.auth.domain.OAuthProvider;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** OAuth 제공자·oauthId 조합으로 식별되는 회원 엔티티. */
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_provider_oauth_id", columnNames = {"provider", "oauth_id"})
)
/**
 * 소셜 로그인 사용자를 표현하는 회원 엔티티.
 * provider와 oauthId 조합으로 사용자를 식별하고 JWT 권한 판단에 필요한 role을 보관한다.
 */
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OAuthProvider provider;

    @Column(name = "oauth_id", nullable = false, length = 100)
    private String oauthId;

    @Column(length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected User() {
    }

    public User(OAuthProvider provider, String oauthId, String email, String nickname, Role role) {
        this.provider = provider;
        this.oauthId = oauthId;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }

    /**
     * OAuth provider에서 받은 최신 프로필 값으로 사용자 정보를 갱신한다.
     */
    public void updateProfile(String email, String nickname, Role role) {
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }

    /** 백오피스에서 권한만 변경할 때 사용한다. provider·oauthId는 변경하지 않는다. */
    public void updateRole(Role role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public OAuthProvider getProvider() {
        return provider;
    }

    public String getOauthId() {
        return oauthId;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
