package com.eerdem07.mira.gateway.merchants.application.exception;

import java.util.UUID;

public final class MerchantNotFoundException extends RuntimeException {
    public MerchantNotFoundException(UUID merchantId) {
        super("Merchant not found: " + merchantId);
    }
}
