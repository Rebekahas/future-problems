package com.gobison.backend.features.auth.application;

import java.time.Instant;

public record RegisteredAccount(
        String userId,
        String firstName,
        String lastName,
        String email,
        Instant createdAt
) {
}
