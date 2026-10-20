package com.gobison.backend.features.auth.presentation;

import com.gobison.backend.features.auth.application.EmailVerificationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EmailVerificationController.class)
public class EmailVerificationExceptionHandler {

    @ExceptionHandler(EmailVerificationException.class)
    public ResponseEntity<Void> handleVerification(EmailVerificationException exception) {
        HttpStatus status = switch (exception.getReason()) {
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case ALREADY_VERIFIED -> HttpStatus.CONFLICT;
            case MALFORMED_CODE -> HttpStatus.BAD_REQUEST;
            case INCORRECT_CODE -> HttpStatus.valueOf(422);
        };
        return ResponseEntity.status(status).build();
    }

    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    public ResponseEntity<Void> handlePersistenceFailure(RuntimeException exception) {
        return ResponseEntity.internalServerError().build();
    }
}
