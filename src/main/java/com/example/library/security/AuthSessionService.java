package com.example.library.security;

import com.example.library.dto.AuthResponse;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AuthSessionService {

    private final Map<String, SessionUser> sessions = new ConcurrentHashMap<>();

    public SessionUser createSession(AuthResponse user) {
        String token = UUID.randomUUID().toString();
        SessionUser sessionUser = new SessionUser(user.userId(), user.phoneNumber(), user.userName(), token);
        sessions.put(token, sessionUser);
        return sessionUser;
    }

    public Optional<SessionUser> findByToken(String token) {
        return Optional.ofNullable(sessions.get(token));
    }
}
