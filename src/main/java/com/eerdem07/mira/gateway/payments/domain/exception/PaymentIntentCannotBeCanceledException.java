package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.payments.domain.PaymentIntentStatus;
import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class PaymentIntentCannotBeCanceledException extends DomainException {
    public PaymentIntentCannotBeCanceledException(PaymentIntentStatus status) {
        super("PAYMENT_INTENT_CANNOT_BE_CANCELED", "Payment intent cannot be canceled because its status is " + status);
    }
}
