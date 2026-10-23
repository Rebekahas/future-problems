package com.gobison.backend.features.auth.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {RegisterAccountController.class, LoginController.class})
public class MailDeliveryExceptionHandler {
    @ExceptionHandler(MailException.class)
    public ResponseEntity<String> deliveryFailed(MailException exception) {
        return ResponseEntity.status(503)
                .body("Unable to send verification email. Your account is saved; log in to retry.");
    }
}
