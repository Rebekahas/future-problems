package com.gobison.backend.features.auth.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequest(
        @NotNull @Pattern(regexp = "[0-9]{6}") String code) {
}
