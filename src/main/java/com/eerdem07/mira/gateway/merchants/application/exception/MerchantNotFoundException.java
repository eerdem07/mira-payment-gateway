package com.eerdem07.mira.gateway.merchants.application.exception;

import com.eerdem07.mira.gateway.shared.exception.ApplicationException;

import java.util.UUID;

public final class MerchantNotFoundException extends ApplicationException {
    public MerchantNotFoundException(UUID merchantId) {
        super(ErrorType.NOT_FOUND, "MERCHANT_NOT_FOUND", "Merchant not found with id: " + merchantId);
    }
}
