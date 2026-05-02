package com.eerdem07.mira.gateway.merchants.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class MerchantCannotActivateException extends DomainException {
    public MerchantCannotActivateException(String message) {
        super("MERCHANT_CANNOT_ACTIVATE", message);
    }
}
