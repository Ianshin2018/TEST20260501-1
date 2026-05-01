package com.example.library.security;

import com.example.library.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String AUTHENTICATED_USER = "authenticatedUser";

    private final AuthSessionService authSessionService;

    public AuthInterceptor(AuthSessionService authSessionService) {
        this.authSessionService = authSessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("請先登入後再進行借閱或還書。");
        }

        String token = authorization.substring("Bearer ".length()).trim();
        SessionUser sessionUser = authSessionService.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("登入狀態已失效，請重新登入。"));
        request.setAttribute(AUTHENTICATED_USER, sessionUser);
        return true;
    }
}
