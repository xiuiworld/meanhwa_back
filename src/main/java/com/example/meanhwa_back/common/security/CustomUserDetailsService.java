package com.example.meanhwa_back.common.security;

import com.example.meanhwa_back.user.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** JWT subject(userId)로 {@link CustomUserDetails}를 로드한다. */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Long userId = parseUserId(username);
        return userRepository.findById(userId)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private Long parseUserId(String username) {
        try {
            return Long.valueOf(username);
        } catch (NumberFormatException exception) {
            throw new UsernameNotFoundException("Invalid user id: " + username);
        }
    }
}
