package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class CheckoutSessionCanceledException extends DomainException {
    public CheckoutSessionCanceledException(String message) {
        super("SESSION_CANCELED", message);
    }
}
