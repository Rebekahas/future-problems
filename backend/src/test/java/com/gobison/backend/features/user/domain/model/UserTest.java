package com.gobison.backend.features.user.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser@myumanitoba.ca", "hashed-password", "Test", "Student", false);
    }

    @Test
    void shouldCreateUserWithAllFields() {
        assertAll(
                () -> assertEquals("testuser@myumanitoba.ca", user.getEmail()),
                () -> assertEquals("hashed-password", user.getPassword()),
                () -> assertEquals("Test", user.getFirstName()),
                () -> assertEquals("Student", user.getLastName()),
                () -> assertFalse(user.isVerified())
        );
    }

    @Test
    void shouldCreateVerifiedUser() {
        User verifiedUser = new User("verified@myumanitoba.ca", "hashed-password", "Verified", "Student", true);
        assertTrue(verifiedUser.isVerified());
    }

    @Test
    void shouldInitializeVerificationCodeAsNull() {
        assertNull(user.getVerificationCode());
    }

    @Test
    void shouldSetAndGetVerificationCode() {
        user.setVerificationCode("ABC123");
        assertEquals("ABC123", user.getVerificationCode());
    }

    @Test
    void shouldUpdateVerificationCode() {
        user.setVerificationCode("ABC123");
        user.setVerificationCode("XYZ789");
        assertEquals("XYZ789", user.getVerificationCode());
    }

    @Test
    void shouldUpdateFirstName() {
        user.setFirstName("Updated");
        assertEquals("Updated", user.getFirstName());
    }

    @Test
    void shouldUpdateLastName() {
        user.setLastName("UpdatedLastName");
        assertEquals("UpdatedLastName", user.getLastName());
    }

    @Test
    void shouldSetVerifiedToTrue() {
        user.setVerified(true);
        assertTrue(user.isVerified());
    }

    @Test
    void shouldSetVerifiedToFalse() {
        user.setVerified(true);
        user.setVerified(false);
        assertFalse(user.isVerified());
    }

    @Test
    void shouldAllowSettingLastNameToNull() {
        user.setLastName(null);

        assertAll(
                () -> assertNull(user.getLastName())
        );
    }

    @Test
    void shouldKeepEmailAndPasswordUnchangedWhenUpdatingOtherFields() {
        user.setFirstName("NewFirstName");
        user.setLastName("NewLastName");
        user.setVerified(true);
        user.setVerificationCode("NEW123");

        assertAll(
                () -> assertEquals("testuser@myumanitoba.ca", user.getEmail()),
                () -> assertEquals("hashed-password", user.getPassword()),
                () -> assertEquals("NewFirstName", user.getFirstName()),
                () -> assertEquals("NewLastName", user.getLastName()),
                () -> assertTrue(user.isVerified()),
                () -> assertEquals("NEW123", user.getVerificationCode())
        );
    }
}