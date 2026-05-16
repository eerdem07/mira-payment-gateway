package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class InvalidRefundAmountException extends DomainException {

    public InvalidRefundAmountException() {
        super("INVALID_REFUND_AMOUNT", "Refund amount must be greater than zero.");
    }
}
