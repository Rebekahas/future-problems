package com.gobison.backend.features.auth.presentation;

import com.gobison.backend.features.auth.application.EmailVerificationException;
import com.gobison.backend.features.auth.application.EmailVerificationService;
import com.gobison.backend.features.auth.presentation.dto.VerifyEmailRequest;
import com.gobison.backend.features.auth.presentation.dto.VerifyEmailResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
public class EmailVerificationController {

    private final EmailVerificationService verificationService;

    public EmailVerificationController(EmailVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/verify-email")
    public VerifyEmailResponse verifyEmail(Principal principal, @Valid @RequestBody VerifyEmailRequest request) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new EmailVerificationException(EmailVerificationException.Reason.UNAUTHENTICATED);
        }
        verificationService.verifyEmail(principal.getName(), request.code());
        return new VerifyEmailResponse(true);
    }
}
