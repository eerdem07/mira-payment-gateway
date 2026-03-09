package com.eerdem07.mira.gateway.merchants.application.exception;

import com.eerdem07.mira.gateway.shared.exception.ApplicationException;

public class PasswordsDoNotMatchException extends ApplicationException {
    public PasswordsDoNotMatchException(String message) {
        super(ErrorType.BAD_REQUEST, "PASSWORDS_DO_NOT_MATCH", "email or password incorrect!");
    }
}
