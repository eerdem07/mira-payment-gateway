package com.eerdem07.mira.gateway.payments.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class InvalidAuthorizationVoidAmountException extends DomainException {

    public InvalidAuthorizationVoidAmountException() {
        super("INVALID_AUTHORIZATION_VOID_AMOUNT", "Authorization void amount must be greater than zero.");
    }
}
