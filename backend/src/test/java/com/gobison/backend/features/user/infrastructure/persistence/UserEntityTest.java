package com.gobison.backend.features.user.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UserEntityTest {
        @Test
    void constructor_initializesFieldsCorrectly() {
        UserEntity entity = new UserEntity(
                "student@myumanitoba.ca",
                "hashed-password",
                "Carina",
                "Rook",
                true
        );

        assertEquals("student@myumanitoba.ca", entity.getEmail());
        assertEquals("hashed-password", entity.getPassword());
        assertEquals("Carina", entity.getFirstName());
        assertEquals("Rook", entity.getLastName());
        assertTrue(entity.isVerified());
    }

    @Test
    void setVerificationCode_updatesVerificationCode() {
        UserEntity entity = new UserEntity(
                "student@myumanitoba.ca",
                "hashed-password",
                "Carina",
                "Rook",
                false
        );
        entity.setVerificationCode("123456");
        assertEquals("123456", entity.getVerificationCode());
    }

    @Test
    void setFirstName_updatesFirstName() {
        UserEntity entity = new UserEntity(
                "student@myumanitoba.ca",
                "hashed-password",
                "Carina",
                "Rook",
                false
        );

        entity.setFirstName("Leon");
        assertEquals("Leon", entity.getFirstName());
    }

    @Test
    void setLastName_updatesLastName() {
        UserEntity entity = new UserEntity(
                "student@myumanitoba.ca",
                "hashed-password",
                "Sasha",
                "Coles",
                false
        );

        entity.setLastName("Janke");
        assertEquals("Janke", entity.getLastName());
    }

    @Test
    void setVerified_updatesVerificationStatus() {
        UserEntity entity = new UserEntity(
                "student@myumanitoba.ca",
                "hashed-password",
                "Not",
                "Verified",
                false
        );

        entity.setVerified(true);
        assertTrue(entity.isVerified());
    }
}
