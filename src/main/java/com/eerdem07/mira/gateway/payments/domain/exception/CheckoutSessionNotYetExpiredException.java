package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class CheckoutSessionNotYetExpiredException extends DomainException {
    public CheckoutSessionNotYetExpiredException(String message) {
        super("SESSION_NOT_YET_EXPIRED", message);
    }
}
