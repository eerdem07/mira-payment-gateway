package com.eerdem07.mira.gateway.merchants.domain.exception;

import com.eerdem07.mira.gateway.shared.exception.DomainException;

public class InvalidMerchantSuspensionStateException extends DomainException {
    public InvalidMerchantSuspensionStateException() {
        super("INVALID_MERCHANT_SUSPENSION_STATE", "SUSPENDED merchant must have suspendedAt.");
    }
}
