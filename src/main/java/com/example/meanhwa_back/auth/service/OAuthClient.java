package com.example.meanhwa_back.auth.service;

import com.example.meanhwa_back.auth.domain.OAuthProvider;

public interface OAuthClient {
    OAuthProvider getProvider();
}
