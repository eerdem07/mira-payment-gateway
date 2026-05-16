package com.eerdem07.mira.gateway.payments.application.exception;

import com.eerdem07.mira.gateway.shared.exception.ApplicationException;

public class AuthorizationVoidCannotBeCreatedException extends ApplicationException {

    public AuthorizationVoidCannotBeCreatedException(String message) {
        super(ErrorType.CONFLICT, "AUTHORIZATION_VOID_CANNOT_BE_CREATED", message);
    }
}
