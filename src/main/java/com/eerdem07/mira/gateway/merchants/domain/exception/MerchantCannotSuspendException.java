package com.eerdem07.mira.gateway.merchants.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class MerchantCannotSuspendException extends DomainException {
    public MerchantCannotSuspendException(String message) {
        super("MERCHANT_CANNOT_SUSPEND", message);
    }
}
