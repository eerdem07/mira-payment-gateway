package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.payments.domain.PaymentAttemptStatus;
import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class InvalidPaymentAttemptStateTransitionException extends DomainException {

    public InvalidPaymentAttemptStateTransitionException(
            PaymentAttemptStatus currentStatus,
            PaymentAttemptStatus targetStatus
    ) {
        super(
                "PAYMENT_ATTEMPT_INVALID_STATE_TRANSITION",
                "Payment attempt cannot transition from " + currentStatus + " to " + targetStatus + "."
        );
    }
}
