package com.eerdem07.mira.gateway.payments.application.exception;

import com.eerdem07.mira.gateway.shared.exception.ApplicationException;

public class CheckoutSessionNotFoundException extends ApplicationException {
    public CheckoutSessionNotFoundException(String token) {
        super(ErrorType.NOT_FOUND, "CHECKOUT_SESSION_NOT_FOUND", "Checkout session not found with token: " + token);
    }
}
