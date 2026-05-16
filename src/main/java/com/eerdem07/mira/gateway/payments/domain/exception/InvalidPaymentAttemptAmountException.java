package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class InvalidPaymentAttemptAmountException extends DomainException {

    public InvalidPaymentAttemptAmountException() {
        super("PAYMENT_ATTEMPT_INVALID_AMOUNT", "Payment attempt amount must be greater than zero.");
    }
}
