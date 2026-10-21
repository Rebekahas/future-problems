package com.gobison.backend.features.auth.application;

public class EmailVerificationException extends RuntimeException {

    public enum Reason {
        UNAUTHENTICATED,
        ALREADY_VERIFIED,
        MALFORMED_CODE,
        INCORRECT_CODE
    }

    private final Reason reason;

    public EmailVerificationException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
