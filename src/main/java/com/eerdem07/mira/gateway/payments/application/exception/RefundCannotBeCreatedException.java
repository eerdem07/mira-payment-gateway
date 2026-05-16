package com.eerdem07.mira.gateway.payments.application.exception;

import com.eerdem07.mira.gateway.shared.exception.ApplicationException;

public class RefundCannotBeCreatedException extends ApplicationException {

    public RefundCannotBeCreatedException(String message) {
        super(ErrorType.CONFLICT, "REFUND_CANNOT_BE_CREATED", message);
    }
}
