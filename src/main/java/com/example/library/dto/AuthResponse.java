package com.example.library.dto;

public record AuthResponse(Long userId, String phoneNumber, String userName, String authToken) {
}
