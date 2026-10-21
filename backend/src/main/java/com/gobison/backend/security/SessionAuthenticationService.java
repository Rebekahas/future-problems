package com.gobison.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionAuthenticationService {

    private final SecurityContextRepository contextRepository;
    private final SessionAuthenticationStrategy sessionStrategy;

    public SessionAuthenticationService(SecurityContextRepository contextRepository,
                                        SessionAuthenticationStrategy sessionStrategy) {
        this.contextRepository = contextRepository;
        this.sessionStrategy = sessionStrategy;
    }

    // Call only after validating credentials or successfully creating the account.
    public void establishSession(String email, HttpServletRequest request, HttpServletResponse response) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("A Email is required to establish a session.");
        }
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(email, null, List.of());
        sessionStrategy.onAuthentication(authentication, request, response);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
    }
}
