package com.gobison.backend.features.auth.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice(assignableTypes = {RegisterAccountController.class, LoginController.class})
public class MailDeliveryExceptionHandler {
    private static final Logger log =
        LoggerFactory.getLogger(MailDeliveryExceptionHandler.class);
        @ExceptionHandler(MailException.class)
        public ResponseEntity<String> deliveryFailed(MailException exception) {
            log.error("Verification email delivery failed", exception);
        
            return ResponseEntity.status(503)
                    .body("Unable to send verification email. Your account is saved; log in to retry.");
        }
}
