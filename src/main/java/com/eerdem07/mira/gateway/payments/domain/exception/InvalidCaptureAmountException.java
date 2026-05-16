package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class InvalidCaptureAmountException extends DomainException {

    public InvalidCaptureAmountException() {
        super("INVALID_CAPTURE_AMOUNT", "Capture amount must be greater than zero.");
    }
}
