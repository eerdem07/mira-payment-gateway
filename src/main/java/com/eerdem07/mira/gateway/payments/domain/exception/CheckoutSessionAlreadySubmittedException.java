package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class CheckoutSessionAlreadySubmittedException extends DomainException {
    public CheckoutSessionAlreadySubmittedException(String message) {
        super("SESSION_SUBMITTED", message);
    }
}
