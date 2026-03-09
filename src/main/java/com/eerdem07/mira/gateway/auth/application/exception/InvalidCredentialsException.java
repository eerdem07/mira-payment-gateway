package com.eerdem07.mira.gateway.auth.application.exception;

import com.eerdem07.mira.gateway.shared.exception.ApplicationException;

public class InvalidCredentialsException extends ApplicationException {
    public InvalidCredentialsException() {
        super(
                ErrorType.UNAUTHORIZED, "INVALID_CREDENTIALS", "email or password incorrect!");
    }
}
