package com.example.library.service;

import com.example.library.dto.AuthResponse;
import com.example.library.dto.LoginRequest;
import com.example.library.dto.RegisterRequest;
import com.example.library.repository.AuthRepository;
import com.example.library.security.AuthSessionService;
import com.example.library.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthRepository authRepository;
    private final AuthSessionService authSessionService;

    public AuthService(AuthRepository authRepository, AuthSessionService authSessionService) {
        this.authRepository = authRepository;
        this.authSessionService = authSessionService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String phoneNumber = normalize(request.phoneNumber());
        String userName = request.userName().trim();
        Long userId = authRepository.register(phoneNumber, request.password(), userName);
        return new AuthResponse(userId, phoneNumber, userName, null);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        AuthResponse authResponse = authRepository.authenticate(normalize(request.phoneNumber()), request.password());
        SessionUser sessionUser = authSessionService.createSession(authResponse);
        return new AuthResponse(
                authResponse.userId(),
                authResponse.phoneNumber(),
                authResponse.userName(),
                sessionUser.authToken()
        );
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
