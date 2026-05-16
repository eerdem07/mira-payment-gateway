package com.eerdem07.mira.gateway.payments.application.exception;

import com.eerdem07.mira.gateway.shared.exception.ApplicationException;

import java.util.UUID;

public class PaymentAttemptNotFoundException extends ApplicationException {

    public PaymentAttemptNotFoundException(UUID id) {
        super(ErrorType.NOT_FOUND, "PAYMENT_ATTEMPT_NOT_FOUND", "Payment attempt not found with id: " + id);
    }
}
