package com.example.library.security;

import com.example.library.dto.AuthResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AuthSessionService {

    private static final Duration SESSION_TTL = Duration.ofHours(12);

    private final Map<String, StoredSession> sessions = new ConcurrentHashMap<>();

    public SessionUser createSession(AuthResponse user) {
        String token = UUID.randomUUID().toString();
        SessionUser sessionUser = new SessionUser(user.userId(), user.phoneNumber(), user.userName(), token);
        sessions.put(token, new StoredSession(sessionUser, Instant.now().plus(SESSION_TTL)));
        return sessionUser;
    }

    public Optional<SessionUser> findByToken(String token) {
        StoredSession storedSession = sessions.get(token);
        if (storedSession == null) {
            return Optional.empty();
        }
        if (storedSession.isExpired()) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(storedSession.sessionUser());
    }

    private record StoredSession(SessionUser sessionUser, Instant expiresAt) {
        private boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
