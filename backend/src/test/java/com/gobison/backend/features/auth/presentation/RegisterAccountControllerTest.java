package com.gobison.backend.features.auth.presentation;

import com.gobison.backend.features.auth.application.RegisterAccountException;
import com.gobison.backend.features.auth.application.RegisterAccountService;
import com.gobison.backend.features.auth.application.RegisteredAccount;
import com.gobison.backend.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static com.gobison.backend.features.auth.application.RegisterAccountException.Reason.EMAIL_ALREADY_REGISTERED;
import static com.gobison.backend.features.auth.application.RegisterAccountException.Reason.INVALID_REQUEST;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RegisterAccountController.class, properties = "spring.config.import=")
@Import({RegisterAccountExceptionHandler.class, SecurityConfiguration.class})
class RegisterAccountControllerTest {

    private static final String ENDPOINT = "/api/v1/auth/register";

    @Autowired private MockMvc mvc;
    @MockitoBean private RegisterAccountService service;

    @Test
    void returnsCreatedAccountAndTimestamp() throws Exception {
        Instant createdAt = Instant.parse("2026-10-21T22:00:00Z");
        when(service.registerAccount(anyString(), anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(new RegisteredAccount(
                        "ada@example.com", "Ada", "Lovelace", "ada@example.com", createdAt));

        mvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace",
                                 "email":"ada@example.com","password":"password"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("ada@example.com"))
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.lastName").value("Lovelace"))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.length()").value(5));

        verify(service).registerAccount(eq("Ada"), eq("Lovelace"), eq("ada@example.com"), eq("password"),
                any(), any());
        verifyNoMoreInteractions(service);
    }

    @Test
    void rejectsInvalidRequestFieldsWithoutCallingService() throws Exception {
        mvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":" ","lastName":"Lovelace",
                                 "email":"not-an-email","password":"password"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void rejectsMalformedJsonWithoutCallingService() throws Exception {
        mvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void mapsInvalidRegistrationToBadRequest() throws Exception {
        expectRegistrationFailure(INVALID_REQUEST, 400);
    }

    @Test
    void mapsDuplicateEmailToConflict() throws Exception {
        expectRegistrationFailure(EMAIL_ALREADY_REGISTERED, 409);
    }

    private void expectRegistrationFailure(RegisterAccountException.Reason reason, int expectedStatus)
            throws Exception {
        when(service.registerAccount(anyString(), anyString(), anyString(), anyString(), any(), any()))
                .thenThrow(new RegisterAccountException(reason));

        mvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace",
                                 "email":"ada@example.com","password":"password"}
                                """))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().string(""));
    }
}
