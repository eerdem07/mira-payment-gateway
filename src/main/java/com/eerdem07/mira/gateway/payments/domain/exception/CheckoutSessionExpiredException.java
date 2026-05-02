package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class CheckoutSessionExpiredException extends DomainException {
    public CheckoutSessionExpiredException(String message) {
        super("SESSION_EXPIRED", message);
    }
}
