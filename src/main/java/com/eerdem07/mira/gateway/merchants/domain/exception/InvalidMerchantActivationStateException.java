package com.eerdem07.mira.gateway.merchants.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class InvalidMerchantActivationStateException extends DomainException {
    public InvalidMerchantActivationStateException() {
        super("INVALID_MERCHANT_ACTIVATION_STATE", "ACTIVE merchant must have activatedAt.");
    }
}
