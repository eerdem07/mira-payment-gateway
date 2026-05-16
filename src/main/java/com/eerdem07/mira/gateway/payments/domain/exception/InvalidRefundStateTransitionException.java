package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.payments.domain.RefundStatus;
import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class InvalidRefundStateTransitionException extends DomainException {

    public InvalidRefundStateTransitionException(RefundStatus currentStatus, RefundStatus targetStatus) {
        super(
                "INVALID_REFUND_STATE_TRANSITION",
                "Refund cannot transition from " + currentStatus + " to " + targetStatus + "."
        );
    }
}
