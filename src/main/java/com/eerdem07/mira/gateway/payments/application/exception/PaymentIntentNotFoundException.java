package com.eerdem07.mira.gateway.payments.application.exception;

import com.eerdem07.mira.gateway.shared.exception.ApplicationException;

import java.util.UUID;

public class PaymentIntentNotFoundException extends ApplicationException {
    public PaymentIntentNotFoundException(UUID id) {
        super(ErrorType.NOT_FOUND, "PAYMENT_INTENT_NOT_FOUND", "Payment intent not found with id: " + id);
    }
}
