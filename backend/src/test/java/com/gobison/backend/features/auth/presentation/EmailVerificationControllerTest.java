package com.gobison.backend.features.auth.presentation;

import com.gobison.backend.features.auth.application.EmailVerificationException;
import com.gobison.backend.features.auth.application.EmailVerificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.gobison.backend.features.auth.application.EmailVerificationException.Reason.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmailVerificationController.class, properties = "spring.config.import=")
@Import(EmailVerificationExceptionHandler.class)
class EmailVerificationControllerTest {

    private static final String ENDPOINT = "/api/v1/auth/verify-email";

    @Autowired private MockMvc mvc;
    @MockitoBean private EmailVerificationService service;

    @Test
    void usesPrincipalAndReturnsOnlyVerificationStatus() throws Exception {
        mvc.perform(post(ENDPOINT).principal(() -> "jsmith")
                        .header("X-User-Id", "someone-else")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"000042\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"universityVerified\":true}"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(service).verifyEmail("jsmith", "000042");
        verifyNoMoreInteractions(service);
    }

    @Test
    void rejectsAnonymousRequestsEvenWithClaimedIdentityOrBearerHeader() throws Exception {
        mvc.perform(post(ENDPOINT).header("X-User-Id", "jsmith")
                        .header("Authorization", "Bearer unvalidated-token")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"483921\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"code\":\"12345\"}", "{\"code\":\"1234567\"}",
            "{\"code\":\"abcdef\"}", "{\"code\":\"１２３４５６\"}", "{"})
    void rejectsMissingOrMalformedCode(String body) throws Exception {
        mvc.perform(post(ENDPOINT).principal(() -> "jsmith")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void rejectsPrincipalWhoseAccountNoLongerExists() throws Exception {
        expectFailure(new EmailVerificationException(UNAUTHENTICATED), 401);
    }

    @Test
    void rejectsAlreadyVerifiedAccount() throws Exception {
        expectFailure(new EmailVerificationException(ALREADY_VERIFIED), 409);
    }

    @Test
    void rejectsIncorrectOrUnissuedCode() throws Exception {
        expectFailure(new EmailVerificationException(INCORRECT_CODE), 422);
    }

    @Test
    void hidesPersistenceFailureDetails() throws Exception {
        expectFailure(new DataAccessResourceFailureException("private database details"), 500);
    }

    private void expectFailure(RuntimeException exception, int expectedStatus) throws Exception {
        doThrow(exception).when(service).verifyEmail("jsmith", "483921");

        mvc.perform(post(ENDPOINT).principal(() -> "jsmith")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"483921\"}"))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().string(""));
    }
}
