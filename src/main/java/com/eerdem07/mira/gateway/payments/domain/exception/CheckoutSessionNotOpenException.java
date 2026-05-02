package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class CheckoutSessionNotOpenException extends DomainException {
    public CheckoutSessionNotOpenException(String message) {
        super("SESSION_NOT_OPEN", message);
    }
}
