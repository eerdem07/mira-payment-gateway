package com.eerdem07.mira.gateway.payments.application.exception;

import com.eerdem07.mira.gateway.shared.exception.ApplicationException;

public class CaptureCannotBeCreatedException extends ApplicationException {

    public CaptureCannotBeCreatedException(String message) {
        super(ErrorType.CONFLICT, "CAPTURE_CANNOT_BE_CREATED", message);
    }
}
