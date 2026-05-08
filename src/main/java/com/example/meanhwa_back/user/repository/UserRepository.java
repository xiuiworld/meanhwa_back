package com.example.meanhwa_back.user.repository;

import java.util.Optional;

import com.example.meanhwa_back.auth.domain.OAuthProvider;
import com.example.meanhwa_back.user.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndOauthId(OAuthProvider provider, String oauthId);
}
