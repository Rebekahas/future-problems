package com.gobison.backend.features.auth.application;

import com.gobison.backend.features.auth.infrastructure.email.EmailService;
import com.gobison.backend.features.user.domain.model.User;
import com.gobison.backend.features.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mail.MailSendException;

import java.security.SecureRandom;
import java.util.Optional;

import static com.gobison.backend.features.auth.application.EmailVerificationException.Reason.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock private UserRepository repository;
    @Mock private EmailService emailService;
    @Mock private SecureRandom random;

    private EmailVerificationService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(repository, emailService, random);
        user = new User("jsmith", "unchanged-password", "Jane", "Smith", false);
    }

    @Test
    void savesSixDigitCodeBeforeSendingToStoredUniversityAddress() {
        when(repository.findByUmNetId("jsmith")).thenReturn(Optional.of(user));
        when(random.nextInt(1_000_000)).thenReturn(42);

        service.sendVerificationCode("jsmith");

        assertEquals("000042", user.getVerificationCode());
        assertFalse(user.isVerified());
        assertEquals("unchanged-password", user.getPassword());
        var order = inOrder(repository, emailService);
        order.verify(repository).save(user);
        order.verify(emailService).sendVerificationCode("jsmith@myumanitoba.ca", "000042");
    }

    @Test
    void explicitSendingReplacesAnExistingCode() {
        user.setVerificationCode("123456");
        when(repository.findByUmNetId("jsmith")).thenReturn(Optional.of(user));
        when(random.nextInt(1_000_000)).thenReturn(999999);

        service.sendVerificationCode("jsmith");

        assertEquals("999999", user.getVerificationCode());
        verify(emailService).sendVerificationCode("jsmith@myumanitoba.ca", "999999");
    }

    @Test
    void propagatesMailFailureAfterSavingCodeWithoutVerifyingUser() {
        when(repository.findByUmNetId("jsmith")).thenReturn(Optional.of(user));
        when(random.nextInt(1_000_000)).thenReturn(483921);
        doThrow(new MailSendException("SMTP unavailable")).when(emailService)
                .sendVerificationCode("jsmith@myumanitoba.ca", "483921");

        assertThrows(MailSendException.class, () -> service.sendVerificationCode("jsmith"));

        verify(repository).save(user);
        assertEquals("483921", user.getVerificationCode());
        assertFalse(user.isVerified());
    }

    @Test
    void doesNotSendIfSavingCodeFails() {
        when(repository.findByUmNetId("jsmith")).thenReturn(Optional.of(user));
        when(repository.save(user)).thenThrow(new DataAccessResourceFailureException("Unavailable"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.sendVerificationCode("jsmith"));

        verifyNoInteractions(emailService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"jsmith@example.com", "jsmith\r\nBcc: other@example.com"})
    void rejectsMalformedStoredIdentityBeforeSavingOrSending(String storedIdentity) {
        User malformedUser = new User(storedIdentity, "hash", "Jane", "Smith", false);
        when(repository.findByUmNetId("jsmith")).thenReturn(Optional.of(malformedUser));

        assertThrows(IllegalArgumentException.class, () -> service.sendVerificationCode("jsmith"));

        verify(repository, never()).save(any());
        verifyNoInteractions(emailService, random);
    }

    @Test
    void verifiesMatchingCodeAndClearsItInTheSameSave() {
        user.setVerificationCode("000042");
        when(repository.findByUmNetId("jsmith")).thenReturn(Optional.of(user));

        service.verifyEmail("jsmith", "000042");

        assertTrue(user.isVerified());
        assertNull(user.getVerificationCode());
        verify(repository).save(user);
        verifyNoInteractions(emailService);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"999999"})
    void rejectsIncorrectOrMissingStoredCodeWithoutSaving(String storedCode) {
        user.setVerificationCode(storedCode);
        when(repository.findByUmNetId("jsmith")).thenReturn(Optional.of(user));

        assertReason(INCORRECT_CODE, () -> service.verifyEmail("jsmith", "483921"));

        assertFalse(user.isVerified());
        assertEquals(storedCode, user.getVerificationCode());
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"abcdef"})
    void rejectsMalformedCodeForDirectServiceCalls(String code) {
        assertReason(MALFORMED_CODE, () -> service.verifyEmail("jsmith", code));
        verifyNoInteractions(repository, emailService);
    }

    @Test
    void rejectsBothSendingAndVerifyingForAlreadyVerifiedUser() {
        user.setVerified(true);
        when(repository.findByUmNetId("jsmith")).thenReturn(Optional.of(user));

        assertReason(ALREADY_VERIFIED, () -> service.sendVerificationCode("jsmith"));
        assertReason(ALREADY_VERIFIED, () -> service.verifyEmail("jsmith", "483921"));

        verify(repository, never()).save(any());
        verifyNoInteractions(emailService, random);
    }

    @Test
    void rejectsNonexistentUserWithoutSavingOrSending() {
        when(repository.findByUmNetId("missing")).thenReturn(Optional.empty());

        assertReason(UNAUTHENTICATED, () -> service.sendVerificationCode("missing"));
        assertReason(UNAUTHENTICATED, () -> service.verifyEmail("missing", "483921"));

        verify(repository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void rejectsMissingIdentity() {
        assertReason(UNAUTHENTICATED, () -> service.verifyEmail(null, "483921"));
        verifyNoInteractions(repository, emailService);
    }

    private void assertReason(EmailVerificationException.Reason reason, org.junit.jupiter.api.function.Executable action) {
        EmailVerificationException exception = assertThrows(EmailVerificationException.class, action);
        assertEquals(reason, exception.getReason());
    }
}
