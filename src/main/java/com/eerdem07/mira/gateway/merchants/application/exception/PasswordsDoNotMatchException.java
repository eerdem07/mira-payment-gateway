package com.eerdem07.mira.gateway.merchants.application.exception;

import com.eerdem07.mira.gateway.shared.exception.ApplicationException;

public class PasswordsDoNotMatchException extends ApplicationException {
    public PasswordsDoNotMatchException(String message) {
        super("PASSWORD_DO_NOT_MATCH", message);
    }
}
