package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.payments.domain.PaymentIntentStatus;
import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class PaymentIntentCannotCreateCheckoutSessionException extends DomainException {

    public PaymentIntentCannotCreateCheckoutSessionException(PaymentIntentStatus status) {
        super(
                "PAYMENT_INTENT_CANNOT_CREATE_CHECKOUT_SESSION",
                "Checkout session cannot be created because payment intent status is " + status + "."
        );
    }
}
