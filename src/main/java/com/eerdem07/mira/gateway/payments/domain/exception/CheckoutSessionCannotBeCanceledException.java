package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.payments.domain.CheckoutSessionStatus;
import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class CheckoutSessionCannotBeCanceledException extends DomainException {
    public CheckoutSessionCannotBeCanceledException(CheckoutSessionStatus status) {
        super("SESSION_CANNOT_BE_CANCELED", "Checkout session cannot be canceled because its status is " + status);
    }
}
