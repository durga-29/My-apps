package com.example.demo.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class CognitoCache {
    private Map<String, String> refreshTokenToUsername = new ConcurrentHashMap<>();

    public void save(String refreshToken, String username) {
        refreshTokenToUsername.put(refreshToken, username);
    }

    public String getUsername(String refreshToken) {
        return refreshTokenToUsername.get(refreshToken);
    }
}

