package com.gobison.backend.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import static org.junit.jupiter.api.Assertions.*;

class SessionAuthenticationServiceTest {

    private final SessionAuthenticationService service = new SessionAuthenticationService(
            new HttpSessionSecurityContextRepository(), new ChangeSessionIdAuthenticationStrategy());

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void storesOnlyAuthenticatedUmNetIdWithoutCredentials() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        service.establishSession("jsmith", request, new MockHttpServletResponse());

        SecurityContext context = (SecurityContext) request.getSession().getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertTrue(context.getAuthentication().isAuthenticated());
        assertEquals("jsmith", context.getAuthentication().getPrincipal());
        assertNull(context.getAuthentication().getCredentials());
        assertNull(context.getAuthentication().getDetails());
        assertTrue(context.getAuthentication().getAuthorities().isEmpty());
        assertSame(context, SecurityContextHolder.getContext());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsMissingIdentityWithoutCreatingASession(String identity) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThrows(IllegalArgumentException.class,
                () -> service.establishSession(identity, request, new MockHttpServletResponse()));
        assertNull(request.getSession(false));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
