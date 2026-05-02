package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class InvalidCheckoutSessionExpirationException extends DomainException {
    public InvalidCheckoutSessionExpirationException(String message) {
        super("INVALID_EXPIRATION", message);
    }
}
