package com.gobison.backend.features.auth.presentation.dto;

import java.time.Instant;

public record RegisterAccountResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        Instant createdAt
) {
}
