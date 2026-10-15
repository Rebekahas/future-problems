package com.gobison.backend.features.user.infrastructure.persistence;

import com.gobison.backend.features.user.domain.model.User;
import com.gobison.backend.features.user.domain.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserRepositoryImplTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        String umNetId = "test_" + UUID.randomUUID().toString().substring(0, 8);

        User user = new User(
                umNetId,
                "test-hash-not-a-real-password",
                "Test",
                "Student",
                false
        );

        User saved = userRepository.save(user);

        assertEquals(umNetId, saved.getUmNetId());
        assertTrue(userRepository.existsByUmNetId(umNetId));

        Optional<User> result = userRepository.findByUmNetId(umNetId);

        assertTrue(result.isPresent());

        User retrieved = result.get();
        assertEquals("Test", retrieved.getFirstName());
        assertEquals("Student", retrieved.getLastName());
        assertFalse(retrieved.isVerified());

        userRepository.deleteByUmNetId(umNetId);

        assertFalse(userRepository.existsByUmNetId(umNetId));
    }
}