package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class InvalidCheckoutSessionUrlException extends DomainException {
    public InvalidCheckoutSessionUrlException(String message) {
        super("INVALID_URL", message);
    }

    public InvalidCheckoutSessionUrlException(String message, Throwable cause) {
        super("INVALID_URL", message, cause);
    }
}
