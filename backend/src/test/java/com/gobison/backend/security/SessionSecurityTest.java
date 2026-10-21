package com.gobison.backend.security;

import com.gobison.backend.features.auth.application.EmailVerificationService;
import com.gobison.backend.features.auth.infrastructure.email.EmailService;
import com.gobison.backend.features.auth.presentation.EmailVerificationController;
import com.gobison.backend.features.auth.presentation.EmailVerificationExceptionHandler;
import com.gobison.backend.features.user.domain.model.User;
import com.gobison.backend.features.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.*;

import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SessionSecurityTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.config.import=")
class SessionSecurityTest {

    private static final String LOGIN = "/api/v1/auth/login";
    private static final String PROFILE = "/api/v1/users/me/profile";
    private static final String VERIFY = "/api/v1/auth/verify-email";

    @Value("${local.server.port}") private int port;
    @MockitoBean private UserRepository users;
    @MockitoBean private EmailService email;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @AfterEach
    void closeClient() {
        client.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {LOGIN, "/api/v1/auth/register"})
    void establishesCookieAuthenticationAndRestoresPrincipalWithoutCsrf(String endpoint) throws Exception {
        HttpResponse<String> response = send("POST", endpoint, null, "{}");
        assertEquals(204, response.statusCode());
        String setCookie = response.headers().firstValue("Set-Cookie").orElseThrow();
        HttpCookie cookie = HttpCookie.parse(setCookie).getFirst();
        assertEquals("JSESSIONID", cookie.getName());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.getSecure());
        assertEquals("/", cookie.getPath());
        assertNull(cookie.getDomain());
        assertEquals(-1, cookie.getMaxAge());
        assertTrue(setCookie.toLowerCase(Locale.ROOT).contains("samesite=lax"));

        for (String method : List.of("GET", "PATCH")) {
            HttpResponse<String> profile = send(method, PROFILE, sessionCookie(response), "{}");
            assertEquals(200, profile.statusCode());
            assertEquals("jsmith", profile.body());
            assertEquals("86400", profile.headers().firstValue("X-Test-Session-Timeout").orElseThrow());
        }
        verifyNoInteractions(users, email);
    }

    @Test
    void rotatesExistingSessionIdAndRejectsTheOldCookie() throws Exception {
        String original = sessionCookie(send("POST", LOGIN, null, "{}"));
        String rotated = sessionCookie(send("POST", LOGIN, original, "{}"));
        assertNotEquals(original, rotated);
        assertEquals(401, send("GET", PROFILE, original, "").statusCode());
        assertEquals(200, send("GET", PROFILE, rotated, "").statusCode());
    }

    @Test
    void verifiesAnUnverifiedAccountUsingTheSessionAndKeepsTheSession() throws Exception {
        User user = new User("jsmith", "unused", "Jane", "Smith", false);
        user.setVerificationCode("000042");
        when(users.findByUmNetId("jsmith")).thenReturn(Optional.of(user));
        String cookie = sessionCookie(send("POST", LOGIN, null, "{}"));

        HttpResponse<String> result = send("POST", VERIFY, cookie, "{\"code\":\"000042\"}");
        assertEquals(200, result.statusCode());
        assertEquals("{\"universityVerified\":true}", result.body());
        assertTrue(user.isVerified());
        assertNull(user.getVerificationCode());
        verify(users).save(user);
        verifyNoInteractions(email);
        assertEquals(200, send("GET", PROFILE, cookie, "").statusCode());
    }

    @Test
    void rejectsMissingSessionsWithoutRedirectingOrCreatingCookies() throws Exception {
        for (String method : List.of("GET", "PATCH")) {
            HttpResponse<String> response = send(method, PROFILE, null, "{}");
            assertEquals(401, response.statusCode());
            assertTrue(response.headers().firstValue("Location").isEmpty());
            assertTrue(response.headers().firstValue("Set-Cookie").isEmpty());
        }
        assertEquals(401, send("POST", VERIFY, null, "{\"code\":\"000042\"}").statusCode());
        verifyNoInteractions(users, email);
    }

    @Test
    void deniesOtherRoutesAndDoesNotEnableLogout() throws Exception {
        String cookie = sessionCookie(send("POST", LOGIN, null, "{}"));
        for (String endpoint : List.of("/api/v1/rides", "/logout", "/api/v1/auth/logout")) {
            assertEquals(403, send("POST", endpoint, cookie, "{}").statusCode());
        }
        assertEquals(403, send("GET", "/logout", cookie, "").statusCode());
        assertEquals(200, send("GET", PROFILE, cookie, "").statusCode());
    }

    @Test
    void allowsCredentialedCorsOnlyForConfiguredOrigins() throws Exception {
        for (String origin : List.of("http://localhost:3000", "http://127.0.0.1:3000")) {
            HttpResponse<String> response = preflight(origin);
            assertEquals(200, response.statusCode());
            assertEquals(origin, response.headers().firstValue("Access-Control-Allow-Origin").orElseThrow());
            assertEquals("true", response.headers().firstValue("Access-Control-Allow-Credentials").orElseThrow());
            assertEquals("Content-Type", response.headers().firstValue("Access-Control-Allow-Headers").orElseThrow());
        }
        assertEquals(403, preflight("http://untrusted.example").statusCode());
    }

    private HttpResponse<String> preflight(String origin) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri(PROFILE)).timeout(Duration.ofSeconds(5))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", origin).header("Access-Control-Request-Method", "PATCH")
                .header("Access-Control-Request-Headers", "Content-Type").build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> send(String method, String path, String cookie, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(path)).timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body));
        if (cookie != null) {
            request.header("Cookie", cookie);
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private String sessionCookie(HttpResponse<String> response) {
        return response.headers().firstValue("Set-Cookie").orElseThrow().split(";", 2)[0];
    }

    // No application component scanning, database configuration, or real email service.
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
    @Import({SecurityConfiguration.class, SessionAuthenticationService.class, FutureEndpoints.class,
            EmailVerificationController.class, EmailVerificationExceptionHandler.class, EmailVerificationService.class})
    static class TestApplication {}

    // These handlers exist only to exercise the future integration contract.
    @RestController
    static class FutureEndpoints {
        @Autowired private SessionAuthenticationService sessions;

        @PostMapping({LOGIN, "/api/v1/auth/register"})
        void authenticate(HttpServletRequest request, HttpServletResponse response) {
            sessions.establishSession("jsmith", request, response);
            response.setStatus(204);
        }

        @RequestMapping(value = PROFILE, method = {RequestMethod.GET, RequestMethod.PATCH})
        String profile(Principal principal, HttpServletRequest request, HttpServletResponse response) {
            response.setHeader("X-Test-Session-Timeout", String.valueOf(request.getSession(false).getMaxInactiveInterval()));
            return principal.getName();
        }
    }
}
