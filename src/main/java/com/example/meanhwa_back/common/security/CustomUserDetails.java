package com.example.meanhwa_back.common.security;

import java.util.Collection;
import java.util.List;

import com.example.meanhwa_back.user.domain.Role;
import com.example.meanhwa_back.user.domain.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Spring Security용 사용자 principal (userId·role). */
public class CustomUserDetails implements UserDetails {
    private final Long userId;
    private final Role role;

    public CustomUserDetails(User user) {
        this.userId = user.getId();
        this.role = user.getRole();
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId);
    }
}
