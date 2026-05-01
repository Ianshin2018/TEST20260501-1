package com.example.library.security;

public record SessionUser(
        Long userId,
        String phoneNumber,
        String userName,
        String authToken
) {
}
