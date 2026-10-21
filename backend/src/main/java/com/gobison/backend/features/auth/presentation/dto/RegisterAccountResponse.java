package com.gobison.backend.features.auth.presentation.dto;

import java.time.Instant;

public record RegisterAccountResponse(
        String userId,
        String firstName,
        String lastName,
        String email,
        Instant createdAt
) {
}
